package org.jenkinsci.plugins.qywechatwatcher;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.OfflineCause;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;


@Extension
public class WatcherComputerListener extends ComputerListener {

    private final QywechatWatcher qywechat;
    private final String jenkinsRootUrl;

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public WatcherComputerListener() {
        this(new QywechatWatcher(Jenkins.get()), Jenkins.get().getRootUrl());
    }

    public WatcherComputerListener(final QywechatWatcher qywechat, final String jenkinsRootUrl) {
        if (qywechat == null) throw new IllegalArgumentException(
                "No qywechat provided"
        );

        this.qywechat = qywechat;
        this.jenkinsRootUrl = jenkinsRootUrl;
    }

    @Override
    public void onOffline(final Computer c) {
        getNotification().online(false).subject("marked offline").send(c);
    }

    @Override
    public void onOffline(final Computer c, final OfflineCause cause) {
        if (cause == null) {
            onOffline(c);
            return;
        }

        getNotification().online(false).subject("marked offline").body(cause.toString()).send(c);
    }

    @Override
    public void onOnline(final Computer c, final TaskListener listener) {
        getNotification().online(true).subject("marked online").send(c);
    }

    @Override
    public void onTemporarilyOffline(final Computer c, final OfflineCause cause) {
        String causeString = "";
        if (cause != null) {
            causeString = cause.toString();
        }
        getNotification().online(false).subject("marked temporarily offline").body(causeString).send(c);
    }

    @Override
    public void onTemporarilyOnline(final Computer c) {
        getNotification().online(true).subject("marked online (was temporarily offline)").send(c);
    }

    private Notification.Builder getNotification() {
        return new Notification.Builder(qywechat, jenkinsRootUrl);
    }

    private static class Notification extends QywechatWatcherNotification {

        public Notification(final Builder builder) {
            super(builder);
        }

        @Override
        protected String getSubject() {
            return String.format("Computer %s %s", getName(), super.getSubject());
        }

        private static class Builder extends QywechatWatcherNotification.Builder {

            private boolean online;

            public Builder(final QywechatWatcher qywechat, final String jenkinsRootUrl) {
                super(qywechat, jenkinsRootUrl);
            }

            public Builder online(final boolean online) {
                this.online = online;
                return this;
            }

            @Override
            public void send(final Object o) {

                final Computer computer = (Computer) o;

                final WatcherNodeProperty property = getWatcherNodeProperty(computer);

                if (property != null) {

                    final String recipients = this.online
                            ? property.getOnlineAddresses()
                            : property.getOfflineAddresses();
                    this.recipients(recipients);
                }

                url(computer.getUrl());
                name(computer.getDisplayName());

                new Notification(this).send();
            }

            private static WatcherNodeProperty getWatcherNodeProperty(final Computer computer) {
                final Node node = computer.getNode();
                if (node == null) return null;
                final DescribableList<NodeProperty<?>, NodePropertyDescriptor> properties = node.getNodeProperties();
                return properties.get(WatcherNodeProperty.class);
            }
        }
    }
}
