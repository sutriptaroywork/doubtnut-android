import * as functions from 'firebase-functions';
import https = require('https');

const CIRCLECI_API_TOKEN = "894a6ca3b48dd6cae2bf091ae66f52989ede0089"

export const buildApk = functions.region("asia-east2").https.onRequest((request, response) => {

    const user = request.body.user_name

    let variant = 'debugRelease'
    let branch = 'master'

    if (request.body.text.includes('|')) {
        // both variant and branch are present
        variant = request.body.text.split("|")[0]
        branch = request.body.text.split("|")[1]
    } else {
        // only branch is present and we use default variant
        branch = request.body.text
    }


    const responseString = `Build started by *${user}*-\nBranch: ${branch}\nVariant: ${variant}`
    
    response.writeHead(200, {'Content-type': 'application/json'})
    response.write(`{"response_type": "in_channel","text": "${responseString}"}`);

    let job = 'debug-build'

    if (variant == 'debugRelease') job = 'debug-release-build'
    if (variant == 'release') job = 'release-build'

    const data = JSON.stringify({
        "build_parameters": {
            "CIRCLE_JOB": job
        }
    })

    const options = {
        hostname: 'circleci.com',
        path: `/api/v1.1/project/github/class21a/dn-android/tree/${branch}`,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': data.length,
          'Authorization': 'Basic ' + new Buffer(CIRCLECI_API_TOKEN + ':').toString('base64')
        }
    }
    const req = https.request(options, (res) => {
        console.log(`statusCode: ${res.statusCode}`)
      
        res.on('data', (d) => {
            response.end()
        })
    })
      
    req.on('error', (error) => {
          response.end()
    })
      
    req.write(data)
    req.end()
});
