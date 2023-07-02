package com.supertokens.ktor.utils

import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.common.requests.FormField as RequestFormField
import com.supertokens.sdk.recipes.common.models.FormField as RecipeFormField

fun getInvalidFormFields(requestFormFields: List<RequestFormField>, recipeFormFields: List<RecipeFormField>): List<RequestFormField> {
    val invalidFormFields = mutableListOf<RequestFormField>()

    requestFormFields.forEach { field ->
        if (recipeFormFields.firstOrNull { it.id == field.id }?.validate?.invoke(field.value) == false) {
            invalidFormFields.add(field)
        }
    }

    return invalidFormFields
}

fun getEmailFormField(fields: List<RequestFormField>): RequestFormField? = fields.firstOrNull { it.id == EmailPasswordRecipe.FORM_FIELD_EMAIL_ID }
fun getPasswordFormField(fields: List<RequestFormField>): RequestFormField? = fields.firstOrNull { it.id == EmailPasswordRecipe.FORM_FIELD_PASSWORD_ID }