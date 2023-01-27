package $package$.core.users

import $package$.prelude.{*, given}

class UserSpec extends QueryChecks {

  check {
    UserImpl.getUserIdByPhone(dummyPhone)
  }

  check {
    UserImpl.getUserLoginInfoByPhone(dummyPhone)
  }

  check {
    UserImpl.insertUser(
      phone = dummyPhone,
      name = "Fathi",
      birthDate = java.time.LocalDate.now(),
      gender = Gender.Male,
      hashedPassword = "xxxx"
    )
  }

  check {
    UserImpl.deleteUserOtp(dummyUserId)
  }

  check {
    UserImpl.getUserOtp(dummyUserId)
  }

  check {
    UserImpl.insertUserOtp(dummyUserId)
  }

  check {
    UserImpl.setUserVerified(dummyUserId)
  }

}
