from taller2.app.models.forbidden_words import ForbiddenWords

class TestForbiddenWords():
    def setup_class(cls):
        cls.forbidden_words = ForbiddenWords()
        cls.forbidden_words.save()

    def test_get_list_forbidden_words(self):
        assert ForbiddenWords.get_words() == []

    def test_add_forbidden_word(self):
        ForbiddenWords.add_word('LPM')
        ForbiddenWords.add_word('HDP')
        assert self.forbidden_words.get_words() == ['LPM', 'HDP']

    def test_delete_word(self):
        ForbiddenWords.delete_word('LPM')
        assert self.forbidden_words.get_words() == ['HDP']
