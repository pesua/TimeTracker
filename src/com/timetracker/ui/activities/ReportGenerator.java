package com.timetracker.ui.activities;

import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.timetracker.domain.Task;
import com.timetracker.domain.TaskSwitchEvent;
import com.timetracker.domain.persistance.DatabaseHelper;

import java.sql.SQLException;
import java.util.*;

public class ReportGenerator {
    public class AggregatedTaskItem {
        public Task task;
        public long duration;

        private AggregatedTaskItem(Task task, long duration) {
            this.task = task;
            this.duration = duration;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{task=").append(task);
            sb.append(", duration=").append(duration);
            sb.append('}');
            return sb.toString();
        }
    }

    public List<AggregatedTaskItem> generateReport(Date from, Date to, Context context) {
        try {
            List<TaskSwitchEvent> events = getPreparedEvents(from, to, context);

            Map<Task, Long> durations = new HashMap<Task, Long>();
            for (int i = 0; i < events.size(); i++) {
                TaskSwitchEvent event = events.get(i);
                Date endTime = i < (events.size() - 1) ? events.get(i + 1).switchTime : to;
                long duration = endTime.getTime() - event.switchTime.getTime();

                if (durations.containsKey(event.task)) {
                    Long t = durations.get(event.task);
                    durations.put(event.task, t + duration);
                } else {
                    durations.put(event.task, duration);
                }
            }
            List<AggregatedTaskItem> result = new ArrayList<AggregatedTaskItem>(durations.size());
            for (Map.Entry<Task, Long> duration : durations.entrySet()) {
                result.add(new AggregatedTaskItem(duration.getKey(), duration.getValue()));
            }
            Collections.sort(result, new Comparator<AggregatedTaskItem>() {
                @Override
                public int compare(AggregatedTaskItem lhs, AggregatedTaskItem rhs) {
                    return -Long.valueOf(lhs.duration).compareTo(rhs.duration);
                }
            });
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TaskSwitchEvent> getPreparedEvents(Date from, Date to, Context context) throws SQLException {
        List<TaskSwitchEvent> events = getHelper(context).getTaskEvents(from, to);
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        TaskSwitchEvent firstEvent = null;
        Integer firstEventId = events.get(0).id;
        if (firstEventId > 1) {
            firstEvent = getHelper(context).getEventsDao().queryForId(firstEventId - 1);
        }

        if (firstEvent != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(from);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            firstEvent.switchTime = calendar.getTime();
            events.add(0, firstEvent);
        }
        return events;
    }

    private DatabaseHelper getHelper(Context context){
        return OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }
}
