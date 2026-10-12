// ==========================================================================
// Gamana Taxi Platform - Interactive Map & Booking Engine
// OpenStreetMap + Leaflet.js with Sri Lanka Geocoding
// ==========================================================================

let map = null;
let pickupMarker = null;
let dropMarker = null;
let routeLine = null;
let activeMode = 'pickup'; // 'pickup' or 'drop'

// Pre-defined popular places in Sri Lanka
const popularPlaces = {
    'fort':        { name: 'Colombo Fort', lat: 6.9344, lng: 79.8428 },
    'bamba':       { name: 'Bambalapitiya', lat: 6.8896, lng: 79.8567 },
    'kandy':       { name: 'Kandy City', lat: 7.2906, lng: 80.6337 },
    'galle':       { name: 'Galle Fort', lat: 6.0535, lng: 80.2210 },
    'airport':     { name: 'BIA Katunayake Airport', lat: 7.1808, lng: 79.8841 },
    'negombo':     { name: 'Negombo', lat: 7.2088, lng: 79.8358 },
    'matara':      { name: 'Matara', lat: 5.9496, lng: 80.5469 },
    'nuwara':      { name: 'Nuwara Eliya', lat: 6.9497, lng: 80.7891 },
    'jaffna':      { name: 'Jaffna Town', lat: 9.6615, lng: 80.0255 }
};

let currentPickup = { name: 'Colombo Fort', lat: 6.9344, lng: 79.8428 };
let currentDrop   = { name: 'Bambalapitiya', lat: 6.8896, lng: 79.8567 };

// Haversine road km formula
function calculateRoadKm(a, b) {
    if (!a || !b) return 0;
    const R = 6371;
    const rad = d => d * Math.PI / 180;
    const dLat = rad(b.lat - a.lat);
    const dLng = rad(b.lng - a.lng);
    const h = Math.sin(dLat / 2) ** 2 +
              Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
    // 1.3 road winding multiplier
    const dist = 2 * R * Math.asin(Math.sqrt(h)) * 1.3;
    return Math.max(0.5, dist);
}

// Create custom colored markers
function createCustomMarkerIcon(type) {
    const isPickup = type === 'pickup';
    const bg = isPickup ? '#10b981' : '#ef4444';
    const icon = isPickup ? '📍' : '🏁';
    return L.divIcon({
        className: 'custom-leaflet-marker',
        html: `<div style="
            background: ${bg};
            width: 34px;
            height: 34px;
            border-radius: 50%;
            border: 3px solid white;
            box-shadow: 0 4px 10px rgba(0,0,0,0.35);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
            cursor: pointer;
        ">${icon}</div>`,
        iconSize: [34, 34],
        iconAnchor: [17, 34],
        popupAnchor: [0, -34]
    });
}

// Reverse geocoding using OSM Nominatim with fast fallback
async function reverseGeocode(lat, lng, callback) {
    try {
        const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=14&addressdetails=1`;
        const res = await fetch(url, { headers: { 'Accept-Language': 'en' } });
        if (res.ok) {
            const data = await res.json();
            if (data && data.display_name) {
                // Shorten display name to city/neighborhood + district
                const addr = data.address || {};
                const shortName = addr.suburb || addr.town || addr.city || addr.village || addr.neighbourhood || addr.county || data.display_name.split(',')[0];
                const state = addr.state || addr.district || '';
                const result = state ? `${shortName}, ${state}` : shortName;
                callback(result);
                return;
            }
        }
    } catch (e) {
        // Fallback on coordinate label if network fails
    }
    callback(`Location (${lat.toFixed(4)}, ${lng.toFixed(4)})`);
}

// Forward search in Sri Lanka
async function searchSriLankaLocation(query) {
    if (!query || !query.trim()) return;
    const cleanQ = query.trim() + ', Sri Lanka';
    const statusMsg = document.getElementById('mapSearchStatus');
    if (statusMsg) {
        statusMsg.textContent = 'Searching location...';
        statusMsg.classList.remove('d-none');
    }

    try {
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(cleanQ)}&countrycodes=lk&limit=1`;
        const res = await fetch(url, { headers: { 'Accept-Language': 'en' } });
        if (res.ok) {
            const results = await res.json();
            if (results && results.length > 0) {
                const item = results[0];
                const lat = parseFloat(item.lat);
                const lng = parseFloat(item.lon);
                const displayName = item.display_name.split(',')[0] + ', ' + (item.address?.city || item.address?.town || item.address?.state || 'Sri Lanka');

                if (map) {
                    map.flyTo([lat, lng], 14, { duration: 1.2 });
                }

                if (activeMode === 'pickup') {
                    setPickupLocation(displayName, lat, lng);
                } else {
                    setDropLocation(displayName, lat, lng);
                }

                if (statusMsg) {
                    statusMsg.textContent = `Found: ${displayName}`;
                    setTimeout(() => statusMsg.classList.add('d-none'), 3000);
                }
                return;
            }
        }
    } catch (e) {
        console.error(e);
    }

    if (statusMsg) {
        statusMsg.textContent = 'Location not found. Click directly on the map to set pin.';
        setTimeout(() => statusMsg.classList.add('d-none'), 4000);
    }
}

// Update UI fields and recalculate fares
function updateFaresAndRoute() {
    const km = calculateRoadKm(currentPickup, currentDrop);
    const distEl = document.getElementById('estDistance');
    const isSame = (currentPickup.lat === currentDrop.lat && currentPickup.lng === currentDrop.lng);

    if (distEl) {
        distEl.textContent = isSame ? '0.0 km' : km.toFixed(1) + ' km';
    }

    const sameMsg = document.getElementById('sameMsg');
    const requestBtn = document.getElementById('requestBtn');
    if (sameMsg) sameMsg.hidden = !isSame;
    if (requestBtn) requestBtn.disabled = isSame;

    // Update vehicle fare cards
    document.querySelectorAll('.fare').forEach(el => {
        const base = Number(el.dataset.base);
        const perKm = Number(el.dataset.km);
        const fare = base + (km * perKm);
        el.textContent = isSame ? '-' : 'Rs ' + (Math.round(fare / 10) * 10);
    });

    // Update Route Line on Map
    if (map && currentPickup && currentDrop && !isSame) {
        const latlngs = [
            [currentPickup.lat, currentPickup.lng],
            [currentDrop.lat, currentDrop.lng]
        ];

        if (routeLine) {
            routeLine.setLatLngs(latlngs);
        } else {
            routeLine = L.polyline(latlngs, {
                color: '#10b981',
                weight: 5,
                opacity: 0.85,
                dashArray: '8, 8',
                lineCap: 'round'
            }).addTo(map);
        }
    }
}

// Set Pickup Location
function setPickupLocation(name, lat, lng) {
    currentPickup = { name, lat, lng };

    const input = document.getElementById('pickupInput');
    const latInput = document.getElementById('pickupLat');
    const lngInput = document.getElementById('pickupLng');

    if (input) input.value = name;
    if (latInput) latInput.value = lat;
    if (lngInput) lngInput.value = lng;

    if (pickupMarker) {
        pickupMarker.setLatLng([lat, lng]);
        pickupMarker.bindPopup(`<b>📍 Pickup</b><br>${name}`).openPopup();
    } else if (map) {
        pickupMarker = L.marker([lat, lng], {
            icon: createCustomMarkerIcon('pickup'),
            draggable: true
        }).addTo(map);

        pickupMarker.bindPopup(`<b>📍 Pickup</b><br>${name}`).openPopup();

        pickupMarker.on('dragend', function(e) {
            const pos = e.target.getLatLng();
            reverseGeocode(pos.lat, pos.lng, function(locName) {
                setPickupLocation(locName, pos.lat, pos.lng);
            });
        });
    }

    updateFaresAndRoute();
}

// Set Drop-off Location
function setDropLocation(name, lat, lng) {
    currentDrop = { name, lat, lng };

    const input = document.getElementById('dropInput');
    const latInput = document.getElementById('dropLat');
    const lngInput = document.getElementById('dropLng');

    if (input) input.value = name;
    if (latInput) latInput.value = lat;
    if (lngInput) lngInput.value = lng;

    if (dropMarker) {
        dropMarker.setLatLng([lat, lng]);
        dropMarker.bindPopup(`<b>🏁 Drop-off</b><br>${name}`).openPopup();
    } else if (map) {
        dropMarker = L.marker([lat, lng], {
            icon: createCustomMarkerIcon('drop'),
            draggable: true
        }).addTo(map);

        dropMarker.bindPopup(`<b>🏁 Drop-off</b><br>${name}`).openPopup();

        dropMarker.on('dragend', function(e) {
            const pos = e.target.getLatLng();
            reverseGeocode(pos.lat, pos.lng, function(locName) {
                setDropLocation(locName, pos.lat, pos.lng);
            });
        });
    }

    updateFaresAndRoute();
}

// Set Map Picking Mode
function setMapMode(mode) {
    activeMode = mode;
    const btnPickup = document.getElementById('modeBtnPickup');
    const btnDrop   = document.getElementById('modeBtnDrop');
    const modeHint  = document.getElementById('mapModeHint');

    if (btnPickup && btnDrop) {
        if (mode === 'pickup') {
            btnPickup.className = 'btn btn-sm map-pin-btn active-pickup';
            btnDrop.className = 'btn btn-sm btn-outline-secondary map-pin-btn';
            if (modeHint) modeHint.innerHTML = '<span class="text-success fw-bold">🟢 Click anywhere to set Pickup Point</span>';
        } else {
            btnPickup.className = 'btn btn-sm btn-outline-secondary map-pin-btn';
            btnDrop.className = 'btn btn-sm map-pin-btn active-drop';
            if (modeHint) modeHint.innerHTML = '<span class="text-danger fw-bold">🔴 Click anywhere to set Drop-off Point</span>';
        }
    }
}

// Initialize Leaflet Map for Sri Lanka
function initBookingMap() {
    const mapEl = document.getElementById('bookingMap');
    if (!mapEl) return;

    // Center on Western Province / Sri Lanka
    map = L.map('bookingMap', {
        center: [6.9344, 79.8428],
        zoom: 12,
        minZoom: 7,
        maxZoom: 18
    });

    // Sri Lanka bounds restriction
    const sriLankaBounds = L.latLngBounds(
        L.latLng(5.5, 79.0),
        L.latLng(10.2, 82.3)
    );
    map.setMaxBounds(sriLankaBounds);

    // OpenStreetMap tile layer
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    // Place Initial Markers
    setPickupLocation(currentPickup.name, currentPickup.lat, currentPickup.lng);
    setDropLocation(currentDrop.name, currentDrop.lat, currentDrop.lng);

    // Fit bounds to show route
    if (map) {
        const bounds = L.latLngBounds([
            [currentPickup.lat, currentPickup.lng],
            [currentDrop.lat, currentDrop.lng]
        ]);
        map.fitBounds(bounds, { padding: [40, 40] });

        // Ensure Leaflet recalculates dimensions smoothly after DOM paints
        setTimeout(() => { map.invalidateSize(); }, 250);
        setTimeout(() => { map.invalidateSize(); }, 750);
        window.addEventListener('resize', () => { map.invalidateSize(); });
    }

    // Map Click Listener
    map.on('click', function(e) {
        const lat = e.latlng.lat;
        const lng = e.latlng.lng;

        reverseGeocode(lat, lng, function(resolvedName) {
            if (activeMode === 'pickup') {
                setPickupLocation(resolvedName, lat, lng);
                // Switch mode automatically to drop-off for next click
                setMapMode('drop');
            } else {
                setDropLocation(resolvedName, lat, lng);
            }
        });
    });
}

// GPS Current Location
function locatePassengerGps() {
    if (!navigator.geolocation) {
        alert('Geolocation is not supported by your browser.');
        return;
    }

    const btn = document.getElementById('gpsLocateBtn');
    if (btn) btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Locating...';

    navigator.geolocation.getCurrentPosition(
        function(pos) {
            const lat = pos.coords.latitude;
            const lng = pos.coords.longitude;

            reverseGeocode(lat, lng, function(locName) {
                setPickupLocation('📍 My Location (' + locName + ')', lat, lng);
                if (map) map.flyTo([lat, lng], 15);
                setMapMode('drop');
                if (btn) btn.innerHTML = '<i class="bi bi-crosshair me-1"></i> Current Location';
            });
        },
        function(err) {
            alert('Unable to retrieve location: ' + err.message);
            if (btn) btn.innerHTML = '<i class="bi bi-crosshair me-1"></i> Current Location';
        },
        { timeout: 10000, enableHighAccuracy: true }
    );
}

// Select a predefined hub
function pickHub(hubKey) {
    const hub = popularPlaces[hubKey];
    if (!hub) return;

    if (map) {
        map.flyTo([hub.lat, hub.lng], 13);
    }

    if (activeMode === 'pickup') {
        setPickupLocation(hub.name, hub.lat, hub.lng);
        setMapMode('drop');
    } else {
        setDropLocation(hub.name, hub.lat, hub.lng);
    }
}

// DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    initBookingMap();

    // Map Search listeners
    const searchBtn = document.getElementById('mapSearchBtn');
    const searchInput = document.getElementById('mapSearchInput');

    if (searchBtn && searchInput) {
        searchBtn.addEventListener('click', () => {
            searchSriLankaLocation(searchInput.value);
        });

        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchSriLankaLocation(searchInput.value);
            }
        });
    }

    // Confirm ride cancellation
    document.querySelectorAll('form.confirm').forEach(f => {
        f.addEventListener('submit', e => {
            if (!confirm('Are you sure you want to cancel this ride?')) {
                e.preventDefault();
            }
        });
    });
});
