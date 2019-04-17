class RegisterError(Exception):
    pass

class BlankUsernameError(RegisterError):
    def __str__(self):
        return 'The username cannot be blank'

class InvalidEmailError(RegisterError):
    def __str__(self):
        return 'The email is invalid. It must be user@domain'
