// noinspection JSUnresolvedFunction,NpmUsedModulesInstalled
const composer = require('openwhisk-composer')

// noinspection JSUnresolvedVariable,JSUnresolvedFunction
module.exports = composer.seq('__PLACEHOLDER__', '__PLACEHOLDER__', composer.literal({
    'body': {
        'result': 'Execution completed'
    }
}))