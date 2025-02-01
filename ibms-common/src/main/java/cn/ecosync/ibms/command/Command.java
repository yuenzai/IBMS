package cn.ecosync.ibms.command;

/**
 * @author yuenzai
 * @since 2024
 */
public interface Command {
    default boolean validate() {
        return true;
    }
}
