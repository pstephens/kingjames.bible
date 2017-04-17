

describe("kj closure", function() {
    it("should be defined at the global scope", function() {
        expect(kj).not.toEqual(null)
    });
});

describe("svg_polyfill", function() {
    afterEach(reset);

    it("should set to png src when data-png attribute set", function() {
        root().innerHTML = '<img id="t1" src="foo.svg" data-png="bar.png">';

        kj.svg_polyfill();

        expect(document.getElementById('t1').getAttribute('src')).toEqual("bar.png");
    });

    it("should not change img src when data-png not set", function() {
        root().innerHTML = '<img id="t2" src="foo.svg">';

        kj.svg_polyfill();

        expect(document.getElementById('t2').getAttribute('src')).toEqual('foo.svg');
    });
});

describe("setActiveElem", function() {
    afterEach(reset);

    it("should return false when element id not found", function() {
        var x = kj.setActiveElem("foo");

        expect(x).toEqual(false);
    });

    it("should return true when target element id is found", function() {
        root().innerHTML = '<div id="f1" class=""></div><div id="f2" class=""></div>';

        var x = kj.setActiveElem("f2");

        expect(x).toEqual(true);
    });

    it("should set active class on target element", function() {
        root().innerHTML = '<div id="f1" class=""></div><div id="f2" class=""></div>';

        kj.setActiveElem("f2");

        expect(/(\s+|^)active(\s+|$)/.test(document.getElementById('f1').getAttribute('class'))).toEqual(false);
        expect(/(\s+|^)active(\s+|$)/.test(document.getElementById('f2').getAttribute('class'))).toEqual(true);
    });

    it("should set unset active class on previous target element", function() {
        root().innerHTML = '<div id="f1" class=""></div><div id="f2" class=""></div>';

        kj.setActiveElem("f2"); // set f2 active first
        kj.setActiveElem("f1"); // set f1 active next, unsetting f2

        expect(/(\s+|^)active(\s+|$)/.test(document.getElementById('f1').getAttribute('class'))).toEqual(true);
        expect(/(\s+|^)active(\s+|$)/.test(document.getElementById('f2').getAttribute('class'))).toEqual(false);
    });
});

describe("setActiveElemOrMain", function() {
    afterEach(reset);

    it("should set active class on target when id is valid", function() {
        root().innerHTML = '<div id="f1" class=""></div><div id="_main" class=""></div>';

        kj.setActiveElemOrMain("f1"); // set to a valid id

        expect(/(\s+|^)active(\s+|$)/.test(document.getElementById('f1').getAttribute('class'))).toEqual(true);
        expect(/(\s+|^)active(\s+|$)/.test(document.getElementById('_main').getAttribute('class'))).toEqual(false);
    });

    it("should set active class on _main when id is not valid", function() {
        root().innerHTML = '<div id="f1" class=""></div><div id="_main" class=""></div>';

        kj.setActiveElemOrMain("invalid_id"); // set to an invalid id

        expect(/(\s+|^)active(\s+|$)/.test(document.getElementById('f1').getAttribute('class'))).toEqual(false);
        expect(/(\s+|^)active(\s+|$)/.test(document.getElementById('_main').getAttribute('class'))).toEqual(true);
    });
});

function root() {
    return document.getElementById("root");
}

function reset() {
    root().innerHTML = "";
    kj.setActiveElem("_not_a_valid_id_");
}
