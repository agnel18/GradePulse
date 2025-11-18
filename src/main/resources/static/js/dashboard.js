// Global chart storage
let charts = {};

// Theme toggle function - available globally
function toggleTheme() {
    const html = document.documentElement;
    const currentTheme = html.getAttribute('data-theme') || 'light';
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    html.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    const icon = document.getElementById('themeIcon');
    if (icon) {
        icon.className = newTheme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
    }
    if (typeof updateChartsTheme === 'function') {
        updateChartsTheme();
    }
}

// Apply saved theme immediately
const savedTheme = localStorage.getItem('theme') || 'light';
document.documentElement.setAttribute('data-theme', savedTheme);

// Update icon after DOM loads
document.addEventListener('DOMContentLoaded', function() {
    const icon = document.getElementById('themeIcon');
    if (icon) {
        icon.className = savedTheme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
    }
});

function getChartColors() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    return {
        text: isDark ? '#f7fafc' : '#2d3748',
        grid: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'
    };
}

function updateChartsTheme() {
    const colors = getChartColors();
    Object.values(charts).forEach(chart => {
        chart.options.plugins.legend.labels.color = colors.text;
        if (chart.options.scales) {
            if (chart.options.scales.x) { 
                chart.options.scales.x.ticks.color = colors.text; 
                chart.options.scales.x.grid.color = colors.grid; 
            }
            if (chart.options.scales.y) { 
                chart.options.scales.y.ticks.color = colors.text; 
                chart.options.scales.y.grid.color = colors.grid; 
            }
        }
        chart.update();
    });
}

// Initialize charts after everything loads
function initializeCharts(statsData) {
    console.log('Initializing charts with data:', statsData);
    
    if (typeof Chart === 'undefined') {
        console.error('Chart.js not loaded!');
        return;
    }
    
    try {
        const colors = getChartColors();
        
        charts.gender = new Chart(document.getElementById('genderChart').getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Male', 'Female', 'Other'],
                datasets: [{ 
                    data: [statsData.maleCount, statsData.femaleCount, statsData.otherGenderCount], 
                    backgroundColor: ['#4facfe', '#f5576c', '#a8e063'], 
                    borderWidth: 0 
                }]
            },
            options: { 
                responsive: true, 
                maintainAspectRatio: false, 
                plugins: { 
                    legend: { position: 'bottom', labels: { color: colors.text } } 
                } 
            }
        });
        console.log('Gender chart created');

        charts.category = new Chart(document.getElementById('categoryChart').getContext('2d'), {
            type: 'bar',
            data: {
                labels: statsData.categoryLabels,
                datasets: [{ 
                    label: 'Students', 
                    data: statsData.categoryData, 
                    backgroundColor: '#667eea', 
                    borderRadius: 8 
                }]
            },
            options: { 
                responsive: true, 
                maintainAspectRatio: false, 
                plugins: { legend: { display: false } }, 
                scales: { 
                    y: { beginAtZero: true, ticks: { stepSize: 1, color: colors.text }, grid: { color: colors.grid } }, 
                    x: { ticks: { color: colors.text }, grid: { color: colors.grid } } 
                } 
            }
        });
        console.log('Category chart created');

        charts.fee = new Chart(document.getElementById('feeChart').getContext('2d'), {
            type: 'pie',
            data: {
                labels: ['Paid', 'Pending', 'Partial'],
                datasets: [{ 
                    data: [statsData.paidCount, statsData.pendingCount, statsData.partialCount], 
                    backgroundColor: ['#56ab2f', '#f5576c', '#f093fb'], 
                    borderWidth: 0 
                }]
            },
            options: { 
                responsive: true, 
                maintainAspectRatio: false, 
                plugins: { 
                    legend: { position: 'bottom', labels: { color: colors.text } } 
                } 
            }
        });
        console.log('Fee chart created');

        charts.class = new Chart(document.getElementById('classChart').getContext('2d'), {
            type: 'bar',
            data: {
                labels: statsData.classLabels,
                datasets: [{ 
                    label: 'Students', 
                    data: statsData.classData, 
                    backgroundColor: '#764ba2', 
                    borderRadius: 8 
                }]
            },
            options: { 
                responsive: true, 
                maintainAspectRatio: false, 
                indexAxis: 'y', 
                plugins: { legend: { display: false } }, 
                scales: { 
                    x: { beginAtZero: true, ticks: { stepSize: 1, color: colors.text }, grid: { color: colors.grid } }, 
                    y: { ticks: { color: colors.text }, grid: { color: colors.grid } } 
                } 
            }
        });
        console.log('Class chart created');

        charts.attendance = new Chart(document.getElementById('attendanceChart').getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Good (≥80%)', 'Needs Attention (<80%)'],
                datasets: [{ 
                    data: [statsData.aboveEightyPercent, statsData.belowEightyPercent], 
                    backgroundColor: ['#56ab2f', '#ff9a56'], 
                    borderWidth: 0 
                }]
            },
            options: { 
                responsive: true, 
                maintainAspectRatio: false, 
                plugins: { 
                    legend: { position: 'bottom', labels: { color: colors.text } } 
                } 
            }
        });
        console.log('Attendance chart created');
        console.log('All charts initialized successfully');
    } catch (error) {
        console.error('Error initializing charts:', error);
    }
}
