import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IContact, NewContact } from '../contact.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IContact for edit and NewContactFormGroupInput for create.
 */
type ContactFormGroupInput = IContact | PartialWithRequiredKeyOf<NewContact>;

type ContactFormDefaults = Pick<NewContact, 'id'>;

type ContactFormGroupContent = {
  id: FormControl<IContact['id'] | NewContact['id']>;
  nom: FormControl<IContact['nom']>;
  prenom: FormControl<IContact['prenom']>;
  age: FormControl<IContact['age']>;
  address: FormControl<IContact['address']>;
  codepostal: FormControl<IContact['codepostal']>;
};

export type ContactFormGroup = FormGroup<ContactFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ContactFormService {
  createContactFormGroup(contact: ContactFormGroupInput = { id: null }): ContactFormGroup {
    const contactRawValue = {
      ...this.getFormDefaults(),
      ...contact,
    };
    return new FormGroup<ContactFormGroupContent>({
      id: new FormControl(
        { value: contactRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      nom: new FormControl(contactRawValue.nom),
      prenom: new FormControl(contactRawValue.prenom),
      age: new FormControl(contactRawValue.age),
      address: new FormControl(contactRawValue.address),
      codepostal: new FormControl(contactRawValue.codepostal),
    });
  }

  getContact(form: ContactFormGroup): IContact | NewContact {
    return form.getRawValue() as IContact | NewContact;
  }

  resetForm(form: ContactFormGroup, contact: ContactFormGroupInput): void {
    const contactRawValue = { ...this.getFormDefaults(), ...contact };
    form.reset(
      {
        ...contactRawValue,
        id: { value: contactRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): ContactFormDefaults {
    return {
      id: null,
    };
  }
}
