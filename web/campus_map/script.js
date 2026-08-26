// script.js - renders LOCATIONS array (generated) onto a Leaflet map
const map = L.map('map').setView([5.653, -0.185], 15);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '© OpenStreetMap'
}).addTo(map);

const markers = {};
const listEl = document.getElementById('locations-list');
const fromSel = document.getElementById('from');
const toSel = document.getElementById('to');
let currentRoute = null;
// campus boundary coordinates
const campusBoundaryCoords = [
  [5.6673,-0.1772],
  [5.6653,-0.1776],
  [5.6625,-0.1783],
  [5.66,-0.1789],
  [5.66,-0.1783],
  [5.66,-0.1774],
  [5.6599,-0.1759],
  [5.66,-0.1736],
  [5.6599,-0.1722],
  [5.66,-0.171],
  [5.66,-0.1702],
  [5.66,-0.169],
  [5.6598,-0.1689],
  [5.6571,-0.169],
  [5.654,-0.169],
  [5.6531,-0.169],
  [5.6524,-0.1691],
  [5.652,-0.169],
  [5.6498,-0.1691],
  [5.6492,-0.1692],
  [5.6484,-0.1692],
  [5.6479,-0.1693],
  [5.646,-0.1691],
  [5.6451,-0.1689],
  [5.6443,-0.1688],
  [5.6434,-0.1687],
  [5.6425,-0.1688],
  [5.6424,-0.1692],
  [5.6424,-0.1699],
  [5.6424,-0.1707],
  [5.6423,-0.1711],
  [5.6423,-0.1721],
  [5.6423,-0.1754],
  [5.6423,-0.1766],
  [5.6423,-0.1785],
  [5.6407,-0.1782],
  [5.6379,-0.1778],
  [5.6357,-0.1774],
  [5.6315,-0.1771],
  [5.6314,-0.1779],
  [5.631,-0.1783],
  [5.6294,-0.1815],
  [5.6294,-0.1818],
  [5.6285,-0.1834],
  [5.6278,-0.1846],
  [5.6259,-0.1872],
  [5.6253,-0.1886],
  [5.6249,-0.1901],
  [5.6248,-0.1911],
  [5.6245,-0.1917],
  [5.6249,-0.1921],
  [5.6266,-0.1929],
  [5.6269,-0.1932],
  [5.6276,-0.1936],
  [5.6287,-0.1942],
  [5.6297,-0.195],
  [5.6301,-0.1952],
  [5.632,-0.1966],
  [5.6333,-0.1974],
  [5.6343,-0.1981],
  [5.6351,-0.1987],
  [5.6366,-0.1994],
  [5.6371,-0.1999],
  [5.6388,-0.201],
  [5.6394,-0.2013],
  [5.6402,-0.2018],
  [5.6428,-0.2035],
  [5.6435,-0.2041],
  [5.6445,-0.2046],
  [5.645,-0.205],
  [5.6456,-0.2053],
  [5.646,-0.2053],
  [5.6465,-0.2053],
  [5.6467,-0.2053],
  [5.6476,-0.2054],
  [5.6492,-0.2054],
  [5.6496,-0.2054],
  [5.6502,-0.2054],
  [5.6514,-0.2053],
  [5.6536,-0.2054],
  [5.6541,-0.2054],
  [5.6553,-0.2054],
  [5.6561,-0.2054],
  [5.6568,-0.2054],
  [5.6579,-0.2054],
  [5.6585,-0.2054],
  [5.6591,-0.2054],
  [5.6597,-0.2054],
  [5.6601,-0.2049],
  [5.6608,-0.2047],
  [5.6614,-0.204],
  [5.6615,-0.2037],
  [5.6617,-0.2035],
  [5.6619,-0.2032],
  [5.6617,-0.2027],
  [5.6615,-0.2025],
  [5.662,-0.2024],
  [5.6626,-0.2015],
  [5.6627,-0.2011],
  [5.6628,-0.2009],
  [5.6631,-0.2005],
  [5.6633,-0.2],
  [5.6633,-0.1993],
  [5.6637,-0.1987],
  [5.6638,-0.1983],
  [5.6641,-0.1979],
  [5.6642,-0.1975],
  [5.6651,-0.1955],
  [5.6655,-0.1948],
  [5.6663,-0.1926],
  [5.6667,-0.1918],
  [5.6671,-0.1908],
  [5.6674,-0.1903],
  [5.6674,-0.1889],
  [5.6676,-0.1888],
  [5.6677,-0.1879],
  [5.6674,-0.1878],
  [5.6674,-0.1868],
  [5.6673,-0.1835],
  [5.6673,-0.1794],
  [5.6673,-0.1782]
];

// Create campus boundary polygon
const campusBoundary = L.polygon(campusBoundaryCoords, {
  color: "#0056b3",
  weight: 2.5,
  fillColor: "#0056b3",
  fillOpacity: 0.08,
  smoothFactor: 1
}).addTo(map);

campusBoundary.bindPopup("<b>University of Ghana, Legon</b><br>Main Campus");

function addLocationItem(loc) {
  const li = document.createElement('li');
  li.textContent = `${loc.name} (${loc.locationId})`;
  li.addEventListener('click', () => {
    map.setView([loc.lat, loc.lon], 17);
    markers[loc.locationId].openPopup();
  });
  listEl.appendChild(li);
}

function populate() {
  if (typeof LOCATIONS === 'undefined') {
    listEl.innerHTML = '<li>No locations data found (run ExportLocations)</li>';
    return;
  }
  LOCATIONS.forEach(loc => {
    const m = L.marker([loc.lat, loc.lon]).addTo(map).bindPopup(`<b>${loc.name}</b><br/>ID: ${loc.locationId}`);
    markers[loc.locationId] = m;
    addLocationItem(loc);
    const opt1 = document.createElement('option'); opt1.value = loc.locationId; opt1.text = loc.name; fromSel.appendChild(opt1);
    const opt2 = document.createElement('option'); opt2.value = loc.locationId; opt2.text = loc.name; toSel.appendChild(opt2);
  });
}

function showRoute() {
  const fromId = parseInt(fromSel.value);
  const toId = parseInt(toSel.value);
  if (!fromId || !toId || fromId === toId) return alert('Select distinct From and To');
  const from = LOCATIONS.find(l => l.locationId === fromId);
  const to = LOCATIONS.find(l => l.locationId === toId);
  if (!from || !to) return alert('Locations not found');
  if (currentRoute) { map.removeLayer(currentRoute); currentRoute = null; }

  // Prefer the real road-network path computed by RouteEngine.dijkstra and
  // exported by tools.ExportRoute. Previously this function always drew a
  // straight line between the two markers, which ignored the road network
  // entirely and did not correspond to anything the engine had calculated.
  const exported = (typeof ROUTE !== 'undefined') ? ROUTE : null;
  const matchesSelection = exported && exported.length > 1
    && Math.abs(exported[0].lat - from.lat) < 1e-6
    && Math.abs(exported[0].lon - from.lon) < 1e-6
    && Math.abs(exported[exported.length - 1].lat - to.lat) < 1e-6
    && Math.abs(exported[exported.length - 1].lon - to.lon) < 1e-6;

  if (matchesSelection) {
    currentRoute = L.polyline(exported.map(p => [p.lat, p.lon]),
                              {color: '#0056b3', weight: 4}).addTo(map);
    const summary = (typeof ROUTE_SUMMARY !== 'undefined') ? ROUTE_SUMMARY : '';
    currentRoute.bindPopup('<b>Shortest path</b><br/>' + summary
      + '<br/>' + exported.map(p => p.name).join(' &rarr; ')).openPopup();
  } else {
    // No exported path for this pair. Draw the straight line, but say so, so a
    // direct line is never mistaken for a computed route.
    currentRoute = L.polyline([[from.lat, from.lon], [to.lat, to.lon]],
                              {color: 'grey', weight: 2, dashArray: '6 6'}).addTo(map);
    currentRoute.bindPopup('Straight line only - no computed route exported for '
      + 'this pair yet. Use "Show Route on Map" in the app.').openPopup();
  }
  map.fitBounds(currentRoute.getBounds(), {padding:[40,40]});
}

document.getElementById('show-route').addEventListener('click', showRoute);
document.getElementById('clear-route').addEventListener('click', () => { if (currentRoute) { map.removeLayer(currentRoute); currentRoute = null; } });

populate();
