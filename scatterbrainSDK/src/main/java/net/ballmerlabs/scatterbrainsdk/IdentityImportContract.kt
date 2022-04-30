package net.ballmerlabs.scatterbrainsdk

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class IdentityImportContract : ActivityResultContract<Int, List<Identity>>() {
    override fun createIntent(context: Context, input: Int?): Intent {
        return Intent().apply {
            component = ScatterbrainApi.IMPORT_IDENTITY_COMPONENT
            putExtra(ScatterbrainApi.EXTRA_NUM_IDENTITIES, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Identity> {
        return intent!!.getParcelableArrayListExtra(ScatterbrainApi.EXTRA_IDENTITY_RESULT)!!
    }
}