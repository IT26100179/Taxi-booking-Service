// ==========================================================================
// Gamana Taxi Platform - Driver Navigation & Route Map (Leaflet.js)
// ==========================================================================

function initDriverMap(containerId) {
    const mapEl = document.getElementById(containerId);
    const dataEl = document.getElementById('driverRouteData');
    if (!mapEl || !dataEl) return;

    const pLat = parseFloat(dataEl.dataset.pickupLat);
    const pLng = parseFloat(dataEl.dataset.pickupLng);
    const pName = dataEl.dataset.pickupName || 'Pickup Location';

    const dLat = parseFloat(dataEl.dataset.dropLat);
    const dLng = parseFloat(dataEl.dataset.dropLng);
    const dName = dataEl.dataset.dropName || 'Drop-off Destination';

    if (isNaN(pLat) || isNaN(pLng) || isNaN(dLat) || isNaN(dLng)) return;

    const map = L.map(containerId, {
        zoomControl: true,
        scrollWheelZoom: true
    });

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap'
    }).addTo(map);

    // Custom Pickup Marker (Green)
    const pickupIcon = L.divIcon({
        className: 'custom-driver-marker',
        html: `<div style="
            background: #10b981;
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
        ">📍</div>`,
        iconSize: [34, 34],
        iconAnchor: [17, 34],
        popupAnchor: [0, -34]
    });

    // Custom Drop-off Marker (Red)
    const dropIcon = L.divIcon({
        className: 'custom-driver-marker',
        html: `<div style="
            background: #ef4444;
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
        ">🏁</div>`,
        iconSize: [34, 34],
        iconAnchor: [17, 34],
        popupAnchor: [0, -34]
    });

    const mPickup = L.marker([pLat, pLng], { icon: pickupIcon }).addTo(map);
    mPickup.bindPopup(`<b>🟢 Pickup Location</b><br>${pName}`).openPopup();

    const mDrop = L.marker([dLat, dLng], { icon: dropIcon }).addTo(map);
    mDrop.bindPopup(`<b>🔴 Drop-off Destination</b><br>${dName}`);

    // Dotted route line between pickup and drop
    L.polyline([[pLat, pLng], [dLat, dLng]], {
        color: '#2563eb',
        weight: 5,
        opacity: 0.85,
        dashArray: '8, 8',
        lineCap: 'round'
    }).addTo(map);

    // Auto-fit bounds
    const bounds = L.latLngBounds([[pLat, pLng], [dLat, dLng]]);
    map.fitBounds(bounds, { padding: [45, 45] });

    // Handle initial rendering & window resize
    setTimeout(() => { map.invalidateSize(); }, 250);
    setTimeout(() => { map.invalidateSize(); }, 750);
    window.addEventListener('resize', () => { map.invalidateSize(); });
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('driverTripMap')) {
        initDriverMap('driverTripMap');
    } else if (document.getElementById('driverOfferMap')) {
        initDriverMap('driverOfferMap');
    }
});
