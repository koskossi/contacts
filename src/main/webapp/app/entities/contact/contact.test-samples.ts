import { IContact, NewContact } from './contact.model';

export const sampleWithRequiredData: IContact = {
  id: 1021,
};

export const sampleWithPartialData: IContact = {
  id: 291,
  prenom: 'visualize North Northeast',
  address: 'Robust Pangender Health',
};

export const sampleWithFullData: IContact = {
  id: 22408,
  nom: 'doloribus Bicycle',
  prenom: 'collaborative Ergonomic faithfully',
  age: 10812,
  address: 'revolutionary katal',
  codepostal: 15943,
};

export const sampleWithNewData: NewContact = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
