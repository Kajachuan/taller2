from ...exceptions.register_error import ShortPasswordError, PasswordConfirmationError

class PasswordValidator(object):
    @classmethod
    def validate(cls, password, confirmation):
        if len(password) < 5:
            raise ShortPasswordError()

        if password != confirmation:
            raise PasswordConfirmationError()
