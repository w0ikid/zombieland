export type ResourceType = 'FOOD' | 'AMMO' | 'MATERIALS';

export interface ResourceDTO {
  id: number;
  type: ResourceType;
  amount: number;
}

export interface DistrictDTO {
  id: number;
  name: string;
  owner: string;
  lat: number;
  lng: number;
  survivalIndex: number;
  isActive: boolean;
  resources: ResourceDTO[];
}

export type OutcomeType = 'success' | 'partial' | 'fail';

export interface SortieOutcome {
  description: string;
  outcome: OutcomeType;
  resources: Record<string, number>;
}