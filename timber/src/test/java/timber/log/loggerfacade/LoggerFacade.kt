package timber.log.loggerfacade

import timber.log.Timber

//This is just example facade object that uses Timber for logging
object LoggerFacade{
    fun d(message: String){
        Timber.d(message)
    }
}
