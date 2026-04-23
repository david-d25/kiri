import {useMemo, useState} from "react";
import {
    Bar,
    BarChart,
    CartesianGrid,
    Legend,
    Line,
    LineChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";
import {useLlmUsageAggregate, useLlmUsageRecent, useLlmUsageSummary} from "@/hooks/llmUsage";
import {LlmUsageDailyAggregateDto} from "@/lib/api/types/llmusage/LlmUsageDailyAggregateDto";
import {LlmUsageSummaryWindowDto} from "@/lib/api/types/llmusage/LlmUsageSummaryDto";
import Panel from "@/components/Panel/Panel";
import TabNavigation from "@/components/TabNavigation/TabNavigation";
import s from "./StatsPage.module.scss";

const TOKEN_COLORS = {
    input: "#60a5fa",
    cacheRead: "#4ade80",
    cacheWrite: "#fbbf24",
    output: "#f87171",
};

type WindowId = "24h" | "7d" | "30d";

const WINDOW_TABS: {id: WindowId; title: string}[] = [
    {id: "24h", title: "24h"},
    {id: "7d", title: "7d"},
    {id: "30d", title: "30d"},
];

const WINDOW_DAYS: Record<WindowId, number> = {
    "24h": 1,
    "7d": 7,
    "30d": 30,
};

function formatTokens(n: number): string {
    if (n >= 1_000_000_000) return (n / 1_000_000_000).toFixed(2) + "B";
    if (n >= 1_000_000) return (n / 1_000_000).toFixed(2) + "M";
    if (n >= 1_000) return (n / 1_000).toFixed(1) + "K";
    return n.toString();
}

function formatHitRate(rate: number): string {
    return (rate * 100).toFixed(1) + "%";
}

function toDate(value: string | number): Date {
    // Backend emits ZonedDateTime as epoch seconds (number).
    // Fall back to string parsing if it ever comes as ISO-8601.
    if (typeof value === "number") return new Date(value * 1000);
    const asNum = Number(value);
    if (!Number.isNaN(asNum) && value !== "") return new Date(asNum * 1000);
    return new Date(value);
}

function formatDate(value: string | number): string {
    return toDate(value).toISOString().slice(5, 10); // MM-DD
}

function formatDateTime(value: string | number): string {
    return toDate(value).toISOString().slice(0, 19).replace("T", " ");
}

export default function StatsPage() {
    const [windowId, setWindowId] = useState<WindowId>("24h");
    const summary = useLlmUsageSummary();
    const aggregate = useLlmUsageAggregate();
    const recent = useLlmUsageRecent(50);

    const dailyChartData = useMemo(
        () => buildDailyChartData(aggregate.data ?? [], WINDOW_DAYS[windowId]),
        [aggregate.data, windowId],
    );

    const selectedWindow = useMemo<LlmUsageSummaryWindowDto | undefined>(
        () => summary.data?.windows.find((w) => w.name === windowId),
        [summary.data, windowId],
    );

    return (
        <div className={s.root}>
            <div className={s.toolbar}>
                <span className={s.toolbarLabel}>Window</span>
                <TabNavigation<WindowId>
                    tabs={WINDOW_TABS}
                    tabId={windowId}
                    onTabChange={setWindowId}
                />
            </div>
            
            <Panel>
                <div className={s.metricsRow}>
                    <Metric label="Requests" value={selectedWindow ? selectedWindow.requestCount.toString() : "—"}/>
                    <Metric label="Raw input" value={selectedWindow ? formatTokens(selectedWindow.rawInputTokens) : "—"}/>
                    <Metric label="Fresh input" value={selectedWindow ? formatTokens(selectedWindow.inputTokens) : "—"}/>
                    <Metric label="Cache read" value={selectedWindow ? formatTokens(selectedWindow.cacheReadInputTokens) : "—"}/>
                    <Metric label="Cache write" value={selectedWindow ? formatTokens(selectedWindow.cacheCreationInputTokens) : "—"}/>
                    <Metric label="Output" value={selectedWindow ? formatTokens(selectedWindow.outputTokens) : "—"}/>
                    <Metric label="Cache hit" value={selectedWindow ? formatHitRate(selectedWindow.cacheHitRate) : "—"}/>
                </div>
            </Panel>

            <Panel>
                <div className={s.panelTitle}>Tokens per day (last {windowId})</div>
                {dailyChartData.length === 0 ? (
                    <div className={s.chartEmpty}>No data</div>
                ) : (
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={dailyChartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color)"/>
                            <XAxis dataKey="day" stroke="var(--text-secondary)"/>
                            <YAxis stroke="var(--text-secondary)" tickFormatter={formatTokens}/>
                            <Tooltip
                                contentStyle={{background: "var(--bg-secondary)", border: "1px solid var(--border-color)"}}
                                formatter={(v: any) => formatTokens(Number(v))}
                            />
                            <Legend/>
                            <Bar stackId="tokens" dataKey="input" name="Fresh input" fill={TOKEN_COLORS.input}/>
                            <Bar stackId="tokens" dataKey="cacheRead" name="Cache read" fill={TOKEN_COLORS.cacheRead}/>
                            <Bar stackId="tokens" dataKey="cacheWrite" name="Cache write" fill={TOKEN_COLORS.cacheWrite}/>
                            <Bar stackId="tokens" dataKey="output" name="Output" fill={TOKEN_COLORS.output}/>
                        </BarChart>
                    </ResponsiveContainer>
                )}
            </Panel>

            <Panel>
                <div className={s.panelTitle}>Cache hit rate per day (last {windowId})</div>
                {dailyChartData.length === 0 ? (
                    <div className={s.chartEmpty}>No data</div>
                ) : (
                    <ResponsiveContainer width="100%" height={260}>
                        <LineChart data={dailyChartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color)"/>
                            <XAxis dataKey="day" stroke="var(--text-secondary)"/>
                            <YAxis
                                stroke="var(--text-secondary)"
                                domain={[0, 1]}
                                tickFormatter={(v: number) => (v * 100).toFixed(0) + "%"}
                            />
                            <Tooltip
                                contentStyle={{background: "var(--bg-secondary)", border: "1px solid var(--border-color)"}}
                                formatter={(v: any) => formatHitRate(Number(v))}
                            />
                            <Line type="monotone" dataKey="cacheHitRate" name="Cache hit rate" stroke={TOKEN_COLORS.cacheRead} strokeWidth={2} dot={false}/>
                        </LineChart>
                    </ResponsiveContainer>
                )}
            </Panel>

            <Panel>
                <div className={s.panelTitle}>Recent requests</div>
                <div className={s.tableScroll}>
                    <table className={s.table}>
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Model</th>
                                <th>Input</th>
                                <th>Cache read</th>
                                <th>Cache write</th>
                                <th>Output</th>
                                <th>Duration</th>
                            </tr>
                        </thead>
                        <tbody>
                            {(recent.data ?? []).map((r) => (
                                <tr key={r.id}>
                                    <td>{formatDateTime(r.timestamp)}</td>
                                    <td>{r.model}</td>
                                    <td>{formatTokens(r.inputTokens)}</td>
                                    <td>{formatTokens(r.cacheReadInputTokens)}</td>
                                    <td>{formatTokens(r.cacheCreationInputTokens)}</td>
                                    <td>{formatTokens(r.outputTokens)}</td>
                                    <td>{r.durationMs}ms</td>
                                </tr>
                            ))}
                            {!recent.data && <tr><td colSpan={7}>Loading…</td></tr>}
                            {recent.data && recent.data.length === 0 && <tr><td colSpan={7}>No requests yet</td></tr>}
                        </tbody>
                    </table>
                </div>
            </Panel>
        </div>
    );
}

function Metric({label, value}: {label: string; value: string}) {
    return (
        <div className={s.metric}>
            <span className={s.metricLabel}>{label}</span>
            <span className={s.metricValue}>{value}</span>
        </div>
    );
}

interface DailyChartPoint {
    day: string;
    input: number;
    cacheRead: number;
    cacheWrite: number;
    output: number;
    rawInput: number;
    cacheHitRate: number;
}

function buildDailyChartData(rows: LlmUsageDailyAggregateDto[], windowDays: number): DailyChartPoint[] {
    const cutoff = new Date();
    cutoff.setUTCHours(0, 0, 0, 0);
    cutoff.setUTCDate(cutoff.getUTCDate() - (windowDays - 1));
    const map = new Map<string, DailyChartPoint>();
    for (const r of rows) {
        const rowDate = toDate(r.day);
        if (rowDate < cutoff) continue;
        const day = formatDate(r.day);
        let p = map.get(day);
        if (!p) {
            p = {day, input: 0, cacheRead: 0, cacheWrite: 0, output: 0, rawInput: 0, cacheHitRate: 0};
            map.set(day, p);
        }
        p.input += r.inputTokens;
        p.cacheRead += r.cacheReadInputTokens;
        p.cacheWrite += r.cacheCreationInputTokens;
        p.output += r.outputTokens;
    }
    const out = Array.from(map.values());
    for (const p of out) {
        p.rawInput = p.input + p.cacheRead + p.cacheWrite;
        p.cacheHitRate = p.rawInput > 0 ? p.cacheRead / p.rawInput : 0;
    }
    out.sort((a, b) => a.day.localeCompare(b.day));
    return out;
}
