// noinspection JSUnresolvedFunction,NpmUsedModulesInstalled
const composer = require('openwhisk-composer')

// noinspection JSUnresolvedVariable,JSUnresolvedFunction
module.exports = composer.try(
    composer.seq(
        composer.literal({
            'body': {
                'url': 'https://images.freeimages.com/images/large-previews/8c4/requiem-for-a-daily-dream-2-1428558.jpg'
            }
        }),
        composer.if('__PLACEHOLDER__',
            composer.if('__PLACEHOLDER__',
                composer.literal({
                    'body': {
                        'result': 'Anger detected'
                    }
                }),
                composer.literal({
                    'body': {
                        'result': 'There is no angry person in the image'
                    }
                })),
            composer.literal({
                'body': {
                    'result': 'The image you provided does not contain a face'
                }
            }))),
    composer.literal({
        'body': {
            'result': 'An error occurred'
        }
    }));