
// Start of Dark-light Mode///////////////////////////////////////////////////////////////////////////
        
const lightBtn = document.getElementById("lightBtn");
const darkBtn = document.getElementById("darkBtn");
const savedTheme = localStorage.getItem("theme");
        
function getChartThemeColors() { 
    const styles = getComputedStyle(document.documentElement); 
    return { textColor: styles .getPropertyValue("--chart-text-color") .trim(), 
                gridColor: styles .getPropertyValue("--chart-grid-color") .trim() 
           }; 
}
       
function updateThemeButtons() {
    if (document.documentElement.classList.contains("dark-mode")) {
            darkBtn.classList.add("active");
            lightBtn.classList.remove("active");
        } else {
            lightBtn.classList.add("active");
            darkBtn.classList.remove("active");
        }
}

lightBtn.addEventListener("click", () => {
    localStorage.setItem("theme", "light");
    location.reload();
});

darkBtn.addEventListener("click", () => {
    localStorage.setItem("theme", "dark");
    location.reload();
});

if(savedTheme ==="dark"){
    document.documentElement.classList.add("dark-mode");
}else{
    document.documentElement.classList.remove("dark-mode");
}

updateThemeButtons();

// End of Dark-light Mode///////////////////////////////////////////////////////////////////////
