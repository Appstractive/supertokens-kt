package com.supertokens.ktor.utils

import com.supertokens.sdk.common.FORM_FIELD_EMAIL_ID
import com.supertokens.sdk.common.FORM_FIELD_NEW_PASSWORD_ID
import com.supertokens.sdk.common.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.common.requests.FormFieldDTO as RequestFormField
import com.supertokens.sdk.recipes.common.models.FormField as RecipeFormField

fun getInvalidFormFields(
    requestFormFields: List<RequestFormField>,
    recipeFormFields: List<RecipeFormField>
): List<RequestFormField> {
  val invalidFormFields = mutableListOf<RequestFormField>()

  requestFormFields.forEach { field ->
    if (recipeFormFields.firstOrNull { it.id == field.id }?.validate?.invoke(field.value) ==
        false) {
      invalidFormFields.add(field)
    }
  }

  return invalidFormFields
}

fun List<RequestFormField>.getField(id: String) = firstOrNull { it.id == id }

fun List<RequestFormField>.getEmailField() = getField(FORM_FIELD_EMAIL_ID)

fun List<RequestFormField>.getPasswordField() = getField(FORM_FIELD_PASSWORD_ID)

fun List<RequestFormField>.getNewPasswordField() = getField(FORM_FIELD_NEW_PASSWORD_ID)
