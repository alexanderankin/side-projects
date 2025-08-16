import { Key } from "react";

export interface Citation {
  id: Key,
  name: string,
  description?: string,
  created_at: string,
  updated_at: string,
}
