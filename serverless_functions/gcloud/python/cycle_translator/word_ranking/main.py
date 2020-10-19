from google.cloud.bigtable import Client
from google.cloud.bigtable import row_filters
import re


# noinspection DuplicatedCode
def gc_functions_handler(request):
    # search for string and table in request
    sentence = None
    ranking_table_name = None
    instance_id = None
    column_name = None
    column_family = None

    if request.args.get('sentence') is not None:
        sentence = request.args.get('sentence')
    else:
        return "Error"

    if request.args.get('ranking_table_name') is not None:
        ranking_table_name = request.args.get('ranking_table_name')
    else:
        return "Error"

    if request.args.get('instance_id') is not None:
        instance_id = request.args.get('instance_id')
    else:
        return "Error"

    if request.args.get('column_name') is not None:
        column_name = request.args.get('column_name')
    else:
        return "Error"

    if request.args.get('column_family') is not None:
        column_family = request.args.get('column_family')
    else:
        return "Error"

    # connect bigtable
    client = Client(admin=True)
    instance = client.instance(instance_id)
    table = instance.table(ranking_table_name)

    # isolate words
    for word in re.findall("[a-zA-Z]+", sentence):
        rank_word(word.lower(), table, column_family, column_name)

    # prepare and return response
    return "Updated"


def rank_word(word, table, family, column):

    # perform update
    row = table.row(word)
    row.increment_cell_value(family, column, 1)
