package com.sword;

import com.google.common.collect.ImmutableMap;

public class Constants {
  
  public static final ImmutableMap<String, String> EZPUBLISH_DOCUMENT_TYPE_TO_PROCESS =
      ImmutableMap.<String, String>builder().put("document", "Document").put("file", "Fichier").put("homepage", "Page d'accueil")
          .put("press_release", "Communiqué de presse").put("speech", "Discours").put("publication", "Publication/Rapport").put("type", "Type")
          .put("webletter", "Weblettre").put("article", "Article").put("newspaper", "Le Journal").put("letter", "Lettre").put("job", "Travail")
          .put("tender", "Offre").put("glossary", "Glossaire").put("term", "").put("collection", "Collection").put("visual", "Visuel")
          .put("quizz", "Quizz").put("question", "Question").put("answer", "Réponse").put("organization", "Organisation").put("position", "Position")
          .put("job_card", "Fiche métier").put("sector", "Secteur").put("contact", "Contact").put("highlight", "Evenement marquant")
          .put("contact_subject", "Contact Sujet").put("zoom", "Zoom").put("did_you_know", "Le saviez vous").put("folder", "Dossier")
          .put("image", "Image").put("video", "Video").put("share_story", "Témoignage").put("link", "Lien").put("crisis_msg", "Message de Crise")
          .put("website", "Actualité Internet").put("patrimony", "Patrimoine").put("nouvelle_classe", "Nouvelle Classe").put("sitemap", "Sitemap")
          .put("list", "Catalogue").put("recruiting", "Recrutement").put("offers", "Offres").put("homepage_collection", "Collection Page Accueil")
          .put("event", "Evenement").put("list_zoom", "Liste Zoom").put("right_column", "Colone de droite")
          .put("link_organization", "Lien Organisation").put("link_module", "Lien module").put("homepage_extranet", "Page d'accueil extranet")
          .put("agenda", "Agenda").put("event_agenda", "Evenement agenda").put("section", "Section").put("exercise_folder", "Dossier exercice")
          .put("practice", "Pratique").put("practice_card", "Fiche pratique").put("faq", "FAQ").put("keyword", "Mot clé")
          .put("exercise_group", "Exercice de groupe").put("exercise_modes", "Modes exercice").put("list_exercise_modes", "Modes exercice liste")
          .put("news", "Actualité").put("tab", "Tableau").put("feed", "Flux").put("region", "Region").put("elected", "ELu")
          .put("elected_directory", "Elu Répertoire").put("elected_tab", "Tableau sélectionné").put("elected_section", "Section  sélectionné")
          .put("elected_title", "Titre sélectionné").put("poll", "Sondage").put("right_column_extranet", "")
          .put("reserved_content", "Contenu réservé").put("quizz_extranet", "Quizz extranet").put("quizz_question", "Question de quizz")
          .put("user_value", "Valeur user").put("contact_internet", "Contact internet").put("event_block", "bloc événement").build();
  
  public static final String METADATA_SEPARATOR = " > ";
  
}
