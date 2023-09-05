import {Component, OnInit, OnDestroy, ViewChild, ChangeDetectorRef, OnChanges} from '@angular/core';
import {ActivatedRoute, Data, ParamMap, Router, RouterModule} from '@angular/router';
import {combineLatest, filter, Observable, Subject, switchMap, tap} from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import {MatButtonModule} from "@angular/material/button";
import {IContact} from "../entities/contact/contact.model";
import {FilterOptions, IFilterOption, IFilterOptions} from "../shared/filter";
import {ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER} from "../config/pagination.constants";
import {ContactService, EntityArrayResponseType} from "../entities/contact/service/contact.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ContactDeleteDialogComponent} from "../entities/contact/delete/contact-delete-dialog.component";
import {ASC, DEFAULT_SORT_DATA, DESC, ITEM_DELETED_EVENT} from "../config/navigation.constants";
import {HttpHeaders} from "@angular/common/http";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator, MatPaginatorIntl} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";


export class UserContact {
  id: number;
  nom: string;
  prenom: string;
  age: any;
  address: string;
  action: any;
  constructor(id: number,nom: string,prenom: string, age: any,address: string,action: any) {
    this.id = id;
    this.nom=  nom;
    this.prenom= prenom;
    this.age= age;
    this.address = address;
    this.action = action;
  }
}

@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  imports: [SharedModule, RouterModule, MatButtonModule],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account: Account | null = null;
  public contact: string;
  private readonly destroy$ = new Subject<void>();


  contacts?: IContact[];
  isLoading = false;

  predicate = 'id';
  ascending = true;
  filters: IFilterOptions = new FilterOptions();

  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;


  /// Table material
  columnsToDisplay: string[] = ['nom','prenom', 'age','address','action']; //TODO make this dynamic somehow //private
  dataSource: MatTableDataSource<any>;
  @ViewChild(MatSort) sort: MatSort = new MatSort();
  @ViewChild(MatPaginator) paginator: MatPaginator = new MatPaginator(new MatPaginatorIntl(), ChangeDetectorRef.prototype);



  constructor(private accountService: AccountService, private router: Router,
              protected contactService: ContactService,
              protected activatedRoute: ActivatedRoute,
              protected modalService: NgbModal
              ) {
   this.contact ='';
    let listeContact: Array<UserContact> = new Array<UserContact>();
    this.dataSource = new MatTableDataSource(listeContact);
  }


  trackId = (_index: number, item: IContact): number => this.contactService.getContactIdentifier(item);


  ngOnInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => (this.account = account));

      if (this.account) {
        this.recupererContacts();
      }
    this.filters.filterChanges.subscribe(filterOptions => this.handleNavigation(1, this.predicate, this.ascending, filterOptions));

  }



  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  rechercher(event: Event) {
    const target = event.target as HTMLButtonElement;
    this.dataSource.filter = target.value.trim().toLowerCase();
    console.log(this.dataSource.filter);
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }


  delete(contact: IContact): void {
    const modalRef = this.modalService.open(ContactDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.contact = contact;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        switchMap(() => this.loadFromBackendWithRouteInformations())
      )
      .subscribe({
        next: (res: EntityArrayResponseType) => {
          this.onResponseSuccess(res);
        },
      });
  }

  recupererContacts(): void {
    this.loadFromBackendWithRouteInformations().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  navigateToWithComponentValues(): void {
    this.handleNavigation(this.page, this.predicate, this.ascending, this.filters.filterOptions);
  }

  navigateToPage(page = this.page): void {
    this.handleNavigation(page, this.predicate, this.ascending, this.filters.filterOptions);
  }

  protected loadFromBackendWithRouteInformations(): Observable<EntityArrayResponseType> {
    return combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data]).pipe(
      tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
      switchMap(() => this.queryBackend(this.page, this.predicate, this.ascending, this.filters.filterOptions))
    );
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page = +(page ?? 1);
    const sort = ('id,asc' ?? [data[DEFAULT_SORT_DATA]]).split(',');
    this.predicate = sort[0];
    this.ascending = sort[1] === ASC;
    this.filters.initializeFromParams(params);
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.contacts = dataFromBody;
    let listeContact: Array<UserContact> = new Array<UserContact>();

    this.contacts.forEach((contact: any)=> {
      if(contact) {
        listeContact.push(new UserContact(contact.id,contact.nom,contact.prenom, contact.age, contact.address, ""));
      }
    })
    // this.pageSize = 5;
    this.dataSource = new MatTableDataSource(listeContact);
  }

  protected fillComponentAttributesFromResponseBody(data: IContact[] | null): IContact[] {
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
  }

  protected queryBackend(
    page?: number,
    predicate?: string,
    ascending?: boolean,
    filterOptions?: IFilterOption[]
  ): Observable<EntityArrayResponseType> {
    this.isLoading = true;
    const pageToLoad: number = page ?? 1;
    const queryObject: any = {
      page: pageToLoad - 1,
      size: this.itemsPerPage,
      sort: this.getSortQueryParam(predicate, ascending),
    };
    filterOptions?.forEach(filterOption => {
      queryObject[filterOption.name] = filterOption.values;
    });
    return this.contactService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(page = this.page, predicate?: string, ascending?: boolean, filterOptions?: IFilterOption[]): void {
    const queryParamsObj: any = {
      page,
      size: this.itemsPerPage,
      sort: this.getSortQueryParam(predicate, ascending),
    };

    filterOptions?.forEach(filterOption => {
      queryParamsObj[filterOption.nameAsQueryParam()] = filterOption.values;
    });

    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: queryParamsObj,
    });
  }

  protected getSortQueryParam(predicate = this.predicate, ascending = this.ascending): string[] {
    const ascendingQueryParam = ascending ? ASC : DESC;
    if (predicate === '') {
      return [];
    } else {
      return [predicate + ',' + ascendingQueryParam];
    }
  }

}
