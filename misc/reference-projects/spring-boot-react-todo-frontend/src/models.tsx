export interface Pageable {
  page?: number;
  size?: number;
}

export interface NewTask {
  id?: string;
  title: string;
  description?: string;
}

export interface Task {
  id: string;
  title: string;
  description?: string;
}

export interface Page<T> {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  size: string;
  totalPages: number;
  totalElements: number;
}
