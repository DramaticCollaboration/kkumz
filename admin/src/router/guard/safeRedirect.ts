export function resolveInternalRedirect(rawRedirect: unknown, fallbackPath: string): string {
  if (typeof rawRedirect !== 'string') {
    return fallbackPath;
  }

  const redirect = rawRedirect.trim();
  if (!redirect.startsWith('/') || redirect.startsWith('//')) {
    return fallbackPath;
  }

  return redirect;
}
