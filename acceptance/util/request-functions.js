const fs = require('fs');
const VectorTile = require('vector-tile').VectorTile;
const Protobuf = require('pbf');
const request = require('superagent');

var download = (url, responseType) => {
    console.log("Sending GET to " + url)
    return request
        .get(url)
        .responseType(responseType)
        .ok(res => true); // checking statusCode is handled by testRequest.  A 4xx/5xx should not automatically fail a test.
}

var addTileToResponse = (response) => {
    return new Promise(
        (resolve, reject) => {
            try {
                var pbf = new Protobuf(new Uint8Array(response.body));
                response.tile = new VectorTile(pbf);
                resolve(response);
            } catch (error) {
                reject(error);
            }
        }
)};

var addTextToResponse = (response) => {
    return new Promise(
        (resolve, reject) => {
            try {
                var text = String.fromCharCode.apply(String, new Uint8Array(response.body));
                response.text = text;
                resolve(response);
            } catch (error) {
                reject(error);
            }
        }
)};

var addJSONToResponse = (response) => {
    return new Promise(
        (resolve, reject) => {
            try {          
                response.json = JSON.parse(response.body);
                resolve(response);
            } catch (error) {
                reject(error);
            }
        }
)}

module.exports = {
    downloadTile: (url) => {
        return download(url, 'arraybuffer')
            .then(addTileToResponse);
    },

    downloadJSON: (url) => {
        return download(url, 'json')
            .then(addJSONToResponse);
    },

    get: (url) => {
        return download(url, 'text')
        .then(addTextToResponse);
    },

    testRequest: (requestPromise, expectedStatus) => {
        it('should return a ' + expectedStatus, done => requestPromise.then(
            (response) => {
                expect(response.statusCode).toBe(expectedStatus);
                done();
            },
            (error) => {
                console.log(error);
                done.fail(error);
            }
        ));
        return requestPromise;
    }

}