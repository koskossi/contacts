export interface IContact {
  id: number;
  nom?: string | null;
  prenom?: string | null;
  age?: number | null;
  address?: string | null;
  codepostal?: number | null;
}

export type NewContact = Omit<IContact, 'id'> & { id: null };


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
