/**
 * TaxiGo Client JavaScript
 * Interactive Fare Estimator & Dynamic UI
 */

document.addEventListener('DOMContentLoaded', function () {
    initFareEstimator();
});

function initFareEstimator() {
    const distanceSlider = document.getElementById('distanceSlider');
    const distanceDisplay = document.getElementById('distanceDisplay');
    const fareDisplay = document.getElementById('estimatedFare');
    const vehicleButtons = document.querySelectorAll('.vehicle-selector-btn');
    let selectedVehicleType = 'Car';

    if (!distanceSlider || !fareDisplay) return;

    // Handle vehicle selection
    vehicleButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            vehicleButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            selectedVehicleType = this.getAttribute('data-type');
            calculateFare();
        });
    });

    // Handle slider change
    distanceSlider.addEventListener('input', function () {
        if (distanceDisplay) {
            distanceDisplay.textContent = this.value + ' km';
        }
        calculateFare();
    });

    function calculateFare() {
        const distance = parseFloat(distanceSlider.value) || 10.0;
        
        // Fetch from live backend API or fallback client-side calculation
        fetch(`/api/estimate-fare?type=${encodeURIComponent(selectedVehicleType)}&distance=${distance}`)
            .then(res => res.json())
            .then(data => {
                if (data && data.fare !== undefined) {
                    fareDisplay.textContent = 'Rs. ' + data.fare.toFixed(2);
                }
            })
            .catch(() => {
                // Client-side fallback calculation matching OOP logic
                let base = 150.0;
                let rate = 120.0;
                if (selectedVehicleType.toLowerCase() === 'van') {
                    base = 250.0;
                    rate = 180.0;
                } else if (selectedVehicleType.toLowerCase() === 'bike') {
                    base = 80.0;
                    rate = 60.0;
                }
                const total = base + (distance * rate);
                fareDisplay.textContent = 'Rs. ' + total.toFixed(2);
            });
    }

    // Initial calculation
    calculateFare();
}
