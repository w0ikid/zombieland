import { UserManager } from 'oidc-client-ts';

export const userManager = new UserManager({
  authority: 'http://zitadel.localhost:8080',
  client_id: '367656138990157828',
  redirect_uri: 'http://localhost:5173/callback',
  response_type: 'code',
  scope: 'openid profile email urn:zitadel:iam:org:project:roles',
});

export function getRoles(user: Awaited<ReturnType<typeof userManager.getUser>>): string[] {
  if (!user) return [];
  const claims = user.profile as Record<string, unknown>;
  // Zitadel кладёт роли в этот claim
  const roles = claims['urn:zitadel:iam:org:project:roles'];
  if (!roles || typeof roles !== 'object') return [];
  return Object.keys(roles as object);
}

export function hasRole(user: Awaited<ReturnType<typeof userManager.getUser>>, role: string): boolean {
  return getRoles(user).includes(role);
}