package guard.grails

import org.codehaus.groovy.grails.compiler.DirectoryWatcher

/**
 * Listener to collect changes by the reloading agent. I believe it needs to be synchronized
 * since the agent runs in a separate thread and might be fire changes while we are consuming
 * changes.
 */
class GuardFileChangeListener implements DirectoryWatcher.FileChangeListener {
    List<File> changes = []
    long lastUpdate = 0

    synchronized void onChange(File file) {
        changes << file
        lastUpdate = new Date().time
    }

    synchronized void onNew(File file) {
        changes << file
        lastUpdate = new Date().time
    }

    synchronized List<File> consumeChanges(long delay) {
        if (new Date().time - lastUpdate < delay) {
            return []
        }

        List<File> newChanges = changes.clone() as List<File>
        changes.clear()
        newChanges
    }
}
