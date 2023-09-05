import {Component, OnInit, OnDestroy, ViewChild, ChangeDetectorRef} from '@angular/core';
import {ActivatedRoute, Data, ParamMap, Router, RouterModule} from '@angular/router';
import {combineLatest, filter, Observable, Subject, switchMap, tap} from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import {MatButtonModule} from "@angular/material/button";
import {IContact, UserContact} from "../entities/contact/contact.model";
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



@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  imports: [SharedModule, RouterModule, MatButtonModule],
})
export default class HomeComponent implements OnInit, OnDestroy {

  // Contact
  contacts?: IContact[];
  exportList: UserContact[] =[];
  account: Account | null = null;

  /// Table material
  columnsToDisplay: string[] =[];
  dataSource: MatTableDataSource<UserContact>;
  @ViewChild(MatSort) sort: MatSort = new MatSort();
  @ViewChild(MatPaginator) paginator: MatPaginator = new MatPaginator(new MatPaginatorIntl(), ChangeDetectorRef.prototype);
  isLoading = false;
  predicate = 'id';
  ascending = true;
  filters: IFilterOptions = new FilterOptions();
  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;

  // Observable
  private readonly destroy$ = new Subject<void>();

  constructor(private accountService: AccountService, private router: Router,
              protected contactService: ContactService,
              protected activatedRoute: ActivatedRoute,
              protected modalService: NgbModal
              ) {
    // Initialisatuion de l'entete de la table contact
    this.columnsToDisplay =  ['nom','prenom', 'age','address','action']
    let listeContact: Array<UserContact> = new Array<UserContact>();
    this.dataSource = new MatTableDataSource(listeContact);
  }




  ngOnInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    // Tester si on est authentifier
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => (this.account = account));
    // récupérer la liste des contacts
    this.recupererContacts();
    this.filters.filterChanges.subscribe(filterOptions => this.handleNavigation(1, this.predicate, this.ascending, filterOptions));
  }



  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Recherche des contacts dans la table de données en fonction de la valeur saisie.
   * @param event L'événement déclenché lors de la saisie dans la barre de recherche.
   */
  rechercher(event: Event) {
    // Récupérer la valeur saisie dans la barre de recherche
    const target = event.target as HTMLButtonElement;
    this.dataSource.filter = target.value.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  /**
   * Supprime un contact après confirmation de l'utilisateur en ouvrant une boîte de dialogue de confirmation.
   * @param contact Le contact à supprimer.
   */
  delete(contact: IContact): void {
    // Ouvre une boîte de dialogue de confirmation de suppression avec les détails du contact.
    const modalRef = this.modalService.open(ContactDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.contact = contact;
    // Écoute la fermeture de la boîte de dialogue (complétion de modalRef.closed).
    modalRef.closed
      .pipe(
        // Filtrer seulement les raisons de fermeture équivalentes à l'événement ITEM_DELETED_EVENT.
        filter(reason => reason === ITEM_DELETED_EVENT),
        // Lorsque l'élément est supprimé, effectuer une action.
        switchMap(() => this.loadFromBackendWithRouteInformations())
      )
      .subscribe({
        // Lorsque la réponse est réussie, exécutez cette fonction de rappel.
        next: (res: EntityArrayResponseType) => {
          this.onResponseSuccess(res);
        },
      });
  }

  /**
   * Récupère les contacts depuis le backend en utilisant les informations de route.
   */
  recupererContacts(): void {
    // Appelle la méthode loadFromBackendWithRouteInformations() et souscrit à son observable.
    this.loadFromBackendWithRouteInformations().subscribe({
      /**
       * Gère la réponse du backend en cas de succès.
       *
       * @param res - La réponse du backend contenant les contacts.
       */
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
    this.exportList = listeContact;
    this.dataSource = new MatTableDataSource(listeContact);
  }

  protected fillComponentAttributesFromResponseBody(data: IContact[] | null): IContact[] {
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
  }

  /**
   * Effectue une requête au backend pour récupérer une liste d'entités paginées.
   *
   * @param page - La page de résultats à récupérer (facultatif, par défaut 1).
   * @param predicate - Le prédicat de tri pour la requête (facultatif).
   * @param ascending - Indique si le tri est ascendant (true) ou descendant (false) (facultatif).
   * @param filterOptions - Un tableau d'options de filtrage pour la requête (facultatif).
   * @returns Un observable contenant la réponse du backend.
   * @protected
   */
  protected queryBackend(
    page?: number,
    predicate?: string,
    ascending?: boolean,
    filterOptions?: IFilterOption[]
  ): Observable<EntityArrayResponseType> {
    // Active le drapeau isLoading pour indiquer le chargement en cours.
    this.isLoading = true;
    const pageToLoad: number = page ?? 1;

    // Construit l'objet de requête avec les paramètres fournis.
    const queryObject: any = {
      page: pageToLoad - 1,
      size: this.itemsPerPage,
      sort: this.getSortQueryParam(predicate, ascending),
    };

    // Ajoute les options de filtrage à l'objet de requête si elles sont spécifiées.
    filterOptions?.forEach(filterOption => {
      queryObject[filterOption.name] = filterOption.values;
    });

    // Appelle le service de contact pour effectuer la requête et retourne l'observable.
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

  /**
   * Cette méthode exporte les données au format CSV à partir d'une liste de données.
   */
  exportCsv() {
    // Convertit la liste de données en une chaîne CSV.
    const csvData = this.convertToCSV(this.exportList);

    // Crée un objet Blob à partir de la chaîne CSV avec le type 'text/csv'.
    const blob = new Blob([csvData], { type: 'text/csv' });
    // Crée une URL pour le Blob.
    const url = window.URL.createObjectURL(blob);

    // Crée un élément de lien (a) pour télécharger le fichier CSV.
    const button = document.createElement('a');

    button.href = url;
    button.download = 'exported-data.csv';
    document.body.appendChild(button);

    // Clique sur le bouton pour déclencher le téléchargement.
    button.click();
    window.URL.revokeObjectURL(url);
  }
  private convertToCSV(data: any[]): string {
    const header = Object.keys(data[0]).join(',');
    const rows = data.map(item => Object.values(item).join(','));
    return `${header}\n${rows.join('\n')}`;
  }


}
