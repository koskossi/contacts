export interface IContact {
  id: number;
  nom?: string | null;
  prenom?: string | null;
  age?: number | null;
  address?: string | null;
  codepostal?: number | null;
}

export type NewContact = Omit<IContact, 'id'> & { id: null };
