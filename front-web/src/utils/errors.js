/**
 * Transforme n'importe quelle erreur axios en un objet unique, quel que soit le type d'erreur :
 * { status, code, message, fieldErrors, isNetworkError }
 *
 * `code` et `message` viennent du format ApiError de l'API (voir common/dto/ApiError.java).
 * `fieldErrors` est converti en objet { champ: message }, pratique pour afficher les erreurs sous les champs.
 */
export function normalizeError(error) {
  const response = error?.response

  if (!response) {
    return {
      status: 0,
      code: 'NETWORK_ERROR',
      message: 'Impossible de joindre le serveur. Vérifiez votre connexion et réessayez.',
      fieldErrors: {},
      isNetworkError: true,
    }
  }

  const data = response.data && typeof response.data === 'object' ? response.data : {}
  const fieldErrors = {}
  for (const item of data.fieldErrors ?? []) {
    if (!fieldErrors[item.field]) fieldErrors[item.field] = item.message
  }

  return {
    status: response.status,
    code: data.code ?? `HTTP_${response.status}`,
    message: data.message ?? 'Une erreur est survenue. Réessayez plus tard.',
    fieldErrors,
    isNetworkError: false,
  }
}
