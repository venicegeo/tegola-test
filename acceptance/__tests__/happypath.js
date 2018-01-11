const rf = require('../util/request-functions');
const env = require('../environment/variables');

describe('A tile request', () => {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 180000
    var rp = rf.testRequest(
        rf.downloadTile(`${env.baseURL}/maps/osm/1/1/3.pbf`),
        200
    );

    it('should have layers in the response', done => rp.then((response) => {
        expect(response.tile).toHaveProperty('layers');
        expect(Object.keys(response.tile.layers).length).toBeGreaterThanOrEqual(1);
        done();
    }));
});

describe('A tile request for a fake map', () => {
    var rp =rf.testRequest(
        rf.get(`${env.baseURL}/maps/foo/1/2/2.pbf`),
        400        
    );

    it('should return "not configured"', done => rp.then((response) => {
        expect(response.text).toMatch(/not configured/);
        done();
    }));

    it("should mention the fake map's name", done => rp.then((response) => {
        expect(response.text).toMatch(/foo/);
        done();
    }));
});

describe('A tile request at a negative row', () => {
    var rp =rf.testRequest(
        rf.get(`${env.baseURL}/maps/osm/10/1/-3.pbf`),
        400        
    );
    
    it('should return "invalid Y value"', done => rp.then((response) => {
        expect(response.text).toMatch(/invalid Y value/i);
        done();
    }));
});

describe('A tile request at a negative column', () => {
    var rp =rf.testRequest(
        rf.get(`${env.baseURL}/maps/osm/10/-1/3.pbf`),
        400        
    );
    
    it('should return "invalid X value"', done => rp.then((response) => {
        expect(response.text).toMatch(/invalid X value/i);
        done();
    }));
});

describe('A tile request at a negative zoom', () => {
    var rp =rf.testRequest(
        rf.get(`${env.baseURL}/maps/osm/-2/1/3.pbf`),
        400        
    );
    
    it('should return "invalid Z value"', done => rp.then((response) => {
        expect(response.text).toMatch(/invalid Z value/i);
        done();
    }));
});

xdescribe('A "land" layer request', () => {
    // var rp = rf.testRequest(
    //     rf.downloadTile(`${env.baseURL}/maps/osm/land/10/1/2.pbf`),
    //     200
    // );

    it('should return the "land" layer', done => rp.then((response) => {
        expect(response.tile).toHaveProperty('layers');
        expect(response.tile.layers).toHaveProperty('land');
        expect(response.tile.layers.land).toHaveProperty('name');
        expect(response.tile.layers.land.name).toEqual('land');
        done();
    }));

    it('should only return one layer', done => rp.then((response) => {
        expect(response.tile).toHaveProperty('layers');
        expect(Object.keys(response.tile.layers).length).toEqual(1);
        done();
    }));
});

describe('A capabilites request', () => {
    var rp = rf.testRequest(
        rf.downloadJSON(`${env.baseURL}/capabilities`),
        200
    );

    it('should return the version', done => rp.then((response) => {
        expect(response.json).toHaveProperty('version');
        done();
    }));
    
    it('should return the expected maps', done => rp.then((response) => {
        expect(response.json).toHaveProperty('maps');
        expect(response.json.maps.length).toBeGreaterThanOrEqual(1);
        var found_osm = false;
        for (var i in response.json.maps) {
            map = response.json.maps[i];
            expect(map).toHaveProperty('name');
            found_osm = found_osm || map.name == "osm";
        }
        expect(found_osm).toBeTruthy();
        done();        
    }));
});