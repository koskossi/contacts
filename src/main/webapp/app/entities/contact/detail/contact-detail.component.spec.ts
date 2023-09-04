import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { ContactDetailComponent } from './contact-detail.component';

describe('Contact Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContactDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: ContactDetailComponent,
              resolve: { contact: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding()
        ),
      ],
    })
      .overrideTemplate(ContactDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load contact on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ContactDetailComponent);

      // THEN
      expect(instance.contact).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
