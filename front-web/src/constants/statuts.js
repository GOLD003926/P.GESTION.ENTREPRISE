export const STATUT_DEMANDE_LABELS = Object.freeze({
  BROUILLON: 'Brouillon',
  EN_VALIDATION_RH: 'Validation RH',
  EN_VALIDATION_FINANCE: 'Validation Finance',
  EN_VALIDATION_DG: 'Validation Direction générale',
  RETOURNEE: 'À modifier',
  REJETEE: 'Rejetée',
  VALIDEE: 'Validée',
  EN_RECRUTEMENT: 'En recrutement',
  POURVUE: 'Pourvue',
  ANNULEE: 'Annulée',
})

export const STATUT_DEMANDE_VARIANTS = Object.freeze({
  BROUILLON: 'neutral',
  EN_VALIDATION_RH: 'info',
  EN_VALIDATION_FINANCE: 'info',
  EN_VALIDATION_DG: 'info',
  RETOURNEE: 'warning',
  REJETEE: 'danger',
  VALIDEE: 'success',
  EN_RECRUTEMENT: 'info',
  POURVUE: 'success',
  ANNULEE: 'neutral',
})

export const DECISION_VALIDATION_LABELS = Object.freeze({
  EN_ATTENTE: 'En attente',
  APPROUVEE: 'Approuvée',
  REJETEE: 'Rejetée',
  RETOURNEE: 'Retournée',
})

export const TYPE_DEMANDE_LABELS = Object.freeze({
  CREATION: 'Création de poste',
  REMPLACEMENT: 'Remplacement',
})

export const MOTIF_DEMANDE_LABELS = Object.freeze({
  CROISSANCE: 'Croissance de l’activité',
  NOUVEAU_PROJET: 'Nouveau projet',
  RESTRUCTURATION: 'Restructuration',
  RENFORT_TEMPORAIRE: 'Renfort temporaire',
  DEMISSION: 'Démission',
  LICENCIEMENT: 'Licenciement',
  RETRAITE: 'Départ à la retraite',
  MUTATION: 'Mutation',
  FIN_CONTRAT: 'Fin de contrat',
  DECES: 'Décès',
  AUTRE: 'Autre',
})

export const STATUT_OFFRE_LABELS = Object.freeze({
  BROUILLON: 'Brouillon',
  PUBLIEE: 'Publiée',
  CLOTUREE: 'Clôturée',
  ANNULEE: 'Annulée',
})

export const STATUT_CANDIDATURE_LABELS = Object.freeze({
  RECUE: 'Reçue',
  PRESELECTIONNEE: 'Présélectionnée',
  REFUSEE_DOSSIER: 'Refusée sur dossier',
  EN_ENTRETIEN: 'En entretien',
  NON_RETENUE: 'Non retenue',
  LISTE_ATTENTE: 'Liste d’attente',
  RETENUE: 'Retenue',
  PROPOSITION_ENVOYEE: 'Proposition envoyée',
  EMBAUCHEE: 'Embauchée',
  DESISTEE: 'Désistée',
})

export const STATUT_PROPOSITION_LABELS = Object.freeze({
  BROUILLON: 'Brouillon',
  ENVOYEE: 'Envoyée',
  DELAI_ACCORDE: 'Délai accordé',
  ACCEPTEE: 'Acceptée',
  REFUSEE: 'Refusée',
  EXPIREE: 'Expirée',
  RETIREE: 'Retirée',
})

export const STATUT_PROPOSITION_VARIANTS = Object.freeze({
  BROUILLON: 'neutral',
  ENVOYEE: 'info',
  DELAI_ACCORDE: 'warning',
  ACCEPTEE: 'success',
  REFUSEE: 'danger',
  EXPIREE: 'danger',
  RETIREE: 'neutral',
})

export const STATUT_ENTRETIEN_LABELS = Object.freeze({
  PLANIFIE: 'Planifié',
  REPORTE: 'Reporté',
  REALISE: 'Réalisé',
  ANNULE: 'Annulé',
  CANDIDAT_ABSENT: 'Candidat absent',
})

export const TYPE_ENTRETIEN_LABELS = Object.freeze({
  ENTRETIEN_RH: 'Entretien RH',
  TEST_TECHNIQUE: 'Test technique',
  ENTRETIEN_FINAL: 'Entretien final',
})

export const DECISION_FINALE_LABELS = Object.freeze({
  RETENU: 'Retenu',
  NON_RETENU: 'Non retenu',
  LISTE_ATTENTE: 'Liste d’attente',
})

export const AVIS_LABELS = Object.freeze({
  FAVORABLE: 'Favorable',
  RESERVE: 'Réservé',
  DEFAVORABLE: 'Défavorable',
})

export const STATUT_EMPLOYE_LABELS = Object.freeze({
  PRE_EMBAUCHE: 'Pré-embauche',
  ACTIF: 'Actif',
  SUSPENDU: 'Suspendu',
  SORTI: 'Sorti',
})

export const MOTIF_AFFECTATION_LABELS = Object.freeze({
  EMBAUCHE: 'Embauche',
  MUTATION: 'Mutation',
  PROMOTION: 'Promotion',
  REORGANISATION: 'Réorganisation',
})

export const TYPE_CONTRAT_LABELS = Object.freeze({
  ESSAI: 'Période d’essai',
  CDD: 'CDD',
  CDI: 'CDI',
})

export const STATUT_CONTRAT_LABELS = Object.freeze({
  BROUILLON: 'Brouillon',
  A_SIGNER: 'À signer',
  ACTIF: 'Actif',
  TERMINE: 'Terminé',
  ANNULE: 'Annulé',
})

export const MOTIF_FIN_CONTRAT_LABELS = Object.freeze({
  ECHEANCE: 'Échéance',
  RENOUVELLEMENT: 'Renouvellement',
  TRANSFORMATION: 'Transformation',
  ESSAI_NON_CONCLUANT: 'Période d’essai non concluante',
  DEMISSION: 'Démission',
  LICENCIEMENT: 'Licenciement',
  RETRAITE: 'Retraite',
  RUPTURE_CONVENTIONNELLE: 'Rupture conventionnelle',
  AUTRE: 'Autre',
})
