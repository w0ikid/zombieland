import type { DistrictDTO, SortieOutcome } from './types';
import { apiFetch } from './client';

const BASE = '/api/v1/districts';

export async function getAllDistricts(): Promise<DistrictDTO[]> {
  const res = await apiFetch(`${BASE}`);
  return res.json();
}

export async function getDistrictById(id: number): Promise<DistrictDTO> {
  const res = await apiFetch(`${BASE}/${id}`);
  return res.json();
}

export async function createDistrict(data: {
  name: string;
  lat: number;
  lng: number;
  survivalIndex?: number;
  isActive?: boolean;
}): Promise<DistrictDTO> {
  const res = await apiFetch(`${BASE}`, {
    method: 'POST',
    body: JSON.stringify({
      name: data.name,
      lat: data.lat,
      lng: data.lng,
      survivalIndex: data.survivalIndex ?? 100,
      isActive: data.isActive ?? true,
    }),
  });
  return res.json();
}

export async function updateDistrict(id: number, data: Partial<DistrictDTO>): Promise<DistrictDTO> {
  const res = await apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
  return res.json();
}

export async function deleteDistrict(id: number): Promise<void> {
  await apiFetch(`${BASE}/${id}`, { method: 'DELETE' });
}

export async function createSortie(id: number, action: string): Promise<SortieOutcome> {
  const res = await apiFetch(`${BASE}/${id}/sortie`, {
    method: 'POST',
    body: action,
  });
  return res.json();
}