class RegisterError(Exception):
    pass

class BlankUsernameError(RegisterError):
    def __str__(self):
        return 'The username cannot be blank'

class InvalidEmailError(RegisterError):
    def __str__(self):
        return 'The email is invalid. It must be user@domain'

class ShortPasswordError(RegisterError):
    def __str__(self):
        return 'The password is too short. It must have at least five characters'

class PasswordConfirmationError(RegisterError):
    def __str__(self):
        return 'The password and the confirmation are not the same'
