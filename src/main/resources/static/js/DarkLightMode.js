
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

// Start of Mobile sidebar toggle///////////////////////////////////////////////////////////////////////

        // (function () {
        //     const toggleBtn = document.getElementById("sidebarToggle");
        //     const backdrop = document.getElementById("sidebarBackdrop");
        //     const sidebar = document.getElementById("sidebar");
        //     if (!toggleBtn || !backdrop || !sidebar) { return; }

        //     function setSidebar(open) {
        //         document.body.classList.toggle("sidebar-open", open);
        //         toggleBtn.setAttribute("aria-expanded", open ? "true" : "false");
        //     }

        //     toggleBtn.addEventListener("click", function () {
        //         setSidebar(!document.body.classList.contains("sidebar-open"));
        //     });
        //     backdrop.addEventListener("click", function () { setSidebar(false); });
        //     document.addEventListener("keydown", function (e) {
        //         if (e.key === "Escape") { setSidebar(false); }
        //     });
        //     // Reset when resizing up to desktop layout
        //     window.matchMedia("(min-width: 992px)").addEventListener("change", function (e) {
        //         if (e.matches) { setSidebar(false); }
        //     });
        // })();

// End of Mobile sidebar toggle///////////////////////////////////////////////////////////////////////
