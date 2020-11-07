package bcrypt

/*
  possibility that certain bcrypt operations can fail due to providing incorrect salt versions
  or number of rounds (eg. > 30 rounds).
 */
case class BcryptException(errorMsg: String) extends Exception(errorMsg)
