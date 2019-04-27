from os import environ
from http import HTTPStatus
from flask import Blueprint, request, abort, session, current_app
from ..models.organization import Organization
from ..models.user import User

organizations = Blueprint('organizations', __name__)

@organizations.route('/organization', methods=['POST'])
def create():
    data = request.get_json(force=True)
    organization_name = data['name']
    current_app.logger.debug('The organization name is: ' + organization_name)

    try:
        owner = User.objects.get(username = session['username'])
    except KeyError:
        abort(HTTPStatus.UNAUTHORIZED)

    new_organization = Organization(organization_name = organization_name, owner = owner)

    try:
        new_organization.save()
        current_app.logger.debug('Organization created')
    except:
        current_app.logger.debug('Failed to create Organization')
        abort(HTTPStatus.BAD_REQUEST)

    return '',HTTPStatus.CREATED
