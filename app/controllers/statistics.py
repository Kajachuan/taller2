from http import HTTPStatus
from datetime import date, timedelta
from flask import Blueprint, jsonify
from ..models.user import User
from ..models.organization import Organization
from ..models.channel import Channel
from ..models.message import Message

statistics = Blueprint('statistics', __name__)

def get_period_statistics(queryset, days):
    period = date.today() - timedelta(days=days)
    queryset = queryset(creation_date__gte=period)
    pipeline = [
        { '$project': {
            'date': { '$dateToString': { 'format': '%Y-%m-%d', 'date': '$creation_date' } }
        } },
        { '$group': {
            '_id': '$date',
            'count': { '$sum': 1 }
        } }
    ]
    stat = list(queryset.aggregate(*pipeline))
    keys = [dict['_id'] for dict in stat]

    for i in range(0,days+1):
        day = (period + timedelta(days=i)).strftime('%Y-%m-%d')
        if day not in keys:
            stat.append({'_id': day, 'count': 0})

    return sorted(stat, key=lambda dict: dict['_id'])

@statistics.route('/statistics/users', methods=['GET'])
def users_statistics():
    data = {}
    user_queryset = User.objects
    data['count'] = user_queryset.count()
    data['week'] = get_period_statistics(user_queryset, 7)
    data['month'] = get_period_statistics(user_queryset, 30)
    return jsonify(data), HTTPStatus.OK

@statistics.route('/statistics/organizations', methods=['GET'])
def organizations_statistics():
    data = {}
    org_queryset = Organization.objects
    data['count'] = org_queryset.count()
    data['week'] = get_period_statistics(org_queryset, 7)
    data['month'] = get_period_statistics(org_queryset, 30)
    return jsonify(data), HTTPStatus.OK

@statistics.route('/statistics/channels', methods=['GET'])
def channels_statistics():
    data = {}
    channel_queryset = Channel.objects
    data['count'] = channel_queryset.count()
    data['week'] = get_period_statistics(channel_queryset, 7)
    data['month'] = get_period_statistics(channel_queryset, 30)
    return jsonify(data), HTTPStatus.OK

@statistics.route('/statistics/messages', methods=['GET'])
def messages_statistics():
    data = {}
    msg_queryset = Message.objects
    data['count'] = msg_queryset.count()
    data['week'] = get_period_statistics(msg_queryset, 7)
    data['month'] = get_period_statistics(msg_queryset, 30)
    return jsonify(data), HTTPStatus.OK
