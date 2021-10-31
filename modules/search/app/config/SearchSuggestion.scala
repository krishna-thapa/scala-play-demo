package config

sealed trait SearchSuggestion {
  val suggestionName: String
  val suggestionColumnName: String = "completion_author"
}

case object CompletionCustomSuggestion extends SearchSuggestion {
  override val suggestionName: String = "completionAuthor"
}

case object PhraseCustomSuggestion extends SearchSuggestion {
  override val suggestionName: String = "phraseAuthor"
}
