const CURRENT_WEEK_KEY = "week";
const NEXT_WEEK_KEY = "week-next";

export default {
  async fetch(request, env) {
    const isNext = new URL(request.url).searchParams.get("week") === "next";
    const raw = await env.MEAL_KV.get(isNext ? NEXT_WEEK_KEY : CURRENT_WEEK_KEY, { type: "json" });
    const days = raw ? raw.days.filter((d) => d.lunch || d.dinner) : [];

    const body = days.length === 0 ? renderEmpty(isNext) : renderPage(raw, days, isNext);
    return new Response(body, {
      status: 200,
      headers: { "content-type": "text/html; charset=UTF-8" },
    });
  },
};

function weekTabs(isNext) {
  return `
    <div class="tabs">
      <a class="tab${isNext ? "" : " active"}" href="?">이번 주</a>
      <a class="tab${isNext ? " active" : ""}" href="?week=next">다음 주</a>
    </div>
  `;
}

function renderPage(payload, days, isNext) {
  return page(`
    <div class="top-bar">
      <span class="brand">🍚 오메오메</span>
      <span class="badge">광주 캠퍼스 식단</span>
    </div>

    <div class="container">
      <div class="title-row">
        <div>
          <h1>${isNext ? "다음 주 식단표" : "이번 주 식단표"}</h1>
          <h2>한국인은 밥심🔥 / ${escapeHtml(payload.rangeLabel)}</h2>
        </div>
      </div>

      ${weekTabs(isNext)}

      <div class="grid">
        ${days.map((d) => dayCard(d, d.date === payload.today)).join("\n")}
      </div>
    </div>
  `);
}

function dayCard(day, isToday) {
  const specials = [day.lunch, day.dinner].filter((m) => m && m.special);

  return `
    <section class="card${isToday ? " today" : ""}">
      ${isToday ? '<div class="ribbon">오늘</div>' : ""}
      <div class="card-head">
        <span class="dow">${escapeHtml(day.label)}</span>
        <span class="date">${escapeHtml(day.shortDate)}</span>
        ${specials.map((m) => `<span class="special" title="${escapeHtml(m.special)}">${escapeHtml(m.special)}</span>`).join("")}
      </div>
      ${mealRow(day.lunch, "lunch")}
      ${mealRow(day.dinner, "dinner")}
    </section>
  `;
}

function mealRow(meal, kind) {
  if (!meal) return "";
  return `
      <div class="meal meal-${kind}">
        <span class="meal-label" style="background:${escapeHtml(meal.color)}">${escapeHtml(meal.label)}</span>
        <ul class="menu">
          ${meal.items.map((item) => `<li>${escapeHtml(item)}</li>`).join("")}
        </ul>
      </div>
  `;
}

function renderEmpty(isNext) {
  return page(`
    <div class="top-bar">
      <span class="brand">🍚 오메오메</span>
    </div>
    <div class="container">
      <h1>${isNext ? "다음 주 식단표" : "이번 주 식단표"}</h1>
      ${weekTabs(isNext)}
      <h2>아직 발행된 식단표가 없습니다</h2>
    </div>
  `);
}

const PAGE_HEAD = `<!doctype html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>오메오메 · 식단표</title>
<style>
  :root {
    --navy: #0e306d;
    --red: #ea002c;
    --orange: #f47725;
    --ink: #16223d;
    --muted: #8b96ab;
    --card-line: #e6eaf1;
  }
  * { box-sizing: border-box; }
  body {
    margin: 0;
    padding-top: 64px;
    background: var(--navy);
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Pretendard, sans-serif;
  }
  .top-bar {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    background: var(--red);
    padding: 14px 24px;
    display: flex;
    align-items: center;
    gap: 12px;
    z-index: 10;
  }
  .top-bar .brand {
    color: #fff;
    font-weight: 800;
    font-size: 18px;
    letter-spacing: 0.02em;
  }
  .top-bar .badge {
    color: #fff;
    opacity: 0.85;
    font-size: 13px;
    border-left: 1px solid rgba(255, 255, 255, 0.4);
    padding-left: 12px;
  }
  .container {
    max-width: 1440px;
    margin: 0 auto;
    padding: 32px 20px 48px;
  }
  .title-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 24px;
  }
  h1 { color: #fff; margin: 0 0 6px; font-size: 26px; }
  h2 { color: var(--muted); margin: 0; font-weight: 500; font-size: 14px; }
  .tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 24px;
  }
  .tab {
    color: rgba(255, 255, 255, 0.7);
    background: rgba(255, 255, 255, 0.08);
    text-decoration: none;
    font-size: 13px;
    font-weight: 700;
    padding: 8px 18px;
    border-radius: 999px;
  }
  .tab.active {
    color: var(--navy);
    background: #fff;
  }
  .grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 16px;
  }
  .card {
    position: relative;
    overflow: hidden;
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  }
  .card.today {
    background: linear-gradient(160deg, #ffe3e7 0%, #ffffff 55%);
    box-shadow: 0 0 0 1px rgba(234, 0, 44, 0.2), 0 16px 32px -10px rgba(234, 0, 44, 0.55);
  }
  .ribbon {
    position: absolute;
    top: 14px;
    right: -34px;
    transform: rotate(45deg);
    background: var(--red);
    color: #fff;
    font-size: 11px;
    font-weight: 800;
    padding: 4px 40px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
  }
  .card-head {
    display: flex;
    align-items: baseline;
    flex-wrap: nowrap;
    gap: 8px;
    margin-bottom: 14px;
  }
  /* 리본은 '오늘' 카드에만 있으므로 그 카드에서만 자리를 비워둔다 */
  .card.today .card-head { padding-right: 28px; }
  .dow { font-size: 20px; font-weight: 800; color: var(--ink); white-space: nowrap; }
  .date { color: var(--muted); font-size: 13px; white-space: nowrap; }
  .special {
    margin-left: auto;
    align-self: center;
    background: var(--orange);
    color: #fff;
    font-weight: 700;
    font-size: 10px;
    padding: 3px 8px;
    border-radius: 999px;
    white-space: nowrap;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .meal { padding: 12px 0; border-top: 1px solid var(--card-line); }
  .meal:first-of-type { border-top: none; padding-top: 0; }
  .meal-lunch .menu { min-height: 180px; }
  .meal-label {
    display: inline-block;
    color: #fff;
    font-weight: 700;
    font-size: 12px;
    padding: 4px 14px;
    border-radius: 999px;
    margin-bottom: 10px;
  }
  .menu { list-style: none; margin: 0; padding: 0; }
  .menu li {
    font-size: 14px;
    color: var(--ink);
    line-height: 1.8;
  }
</style>
</head>
<body>
`;
const PAGE_TAIL = `</body>
</html>`;

function page(body) {
  return PAGE_HEAD + body + PAGE_TAIL;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
}
