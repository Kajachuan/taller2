from taller2.app.models.firebase_api import FirebaseApi

class TestFirebaseApi(object):
    def test_get_response_help(self):
        api = FirebaseApi()
        data = {'help': '@tito help: muestra los comandos disponibles.',\
            'info': '@tito info: muestra información del canal: integrantes, cantidad de mensajes, etc.',\
            'mute': '@tito mute <n>: desactiva respuestas por n segundos.',\
            'me':   '@tito me: muestra información del usuario que envía el mensaje.'}
        response = api.parse_response(data)
        expected_response = '@tito help: muestra los comandos disponibles.\n'\
                            '@tito info: muestra información del canal: integrantes, cantidad de mensajes, etc.\n'\
                            '@tito mute <n>: desactiva respuestas por n segundos.\n'\
                            '@tito me: muestra información del usuario que envía el mensaje.'
        assert response == expected_response

    def test_get_response_info(self):
        api = FirebaseApi()
        data = {'name':'nombre', 'owner': 'creador', 'description':'descripcion'}
        response = api.parse_response(data)
        expected_response = 'Nombre del canal: nombre\nCreador: creador\nDescripcion: descripcion'
        assert response == expected_response
