#include <Helper.h>
#include "SandboxFs.h"
#include "Path.h"

PathItem *keep_items;
PathItem *forbidden_items;
ReplaceItem *replace_items;
int keep_item_count;
int forbidden_item_count;
int replace_item_count;

int add_keep_item(const char *path) {
    char env_name[25];
    sprintf(env_name, "V_KEEP_ITEM_%d", keep_item_count);
    setenv(env_name, path, 1);
    keep_items = (PathItem *) realloc(keep_items,
                                      keep_item_count * sizeof(PathItem) + sizeof(PathItem));
    PathItem &item = keep_items[keep_item_count];
    item.path = strdup(path);
    item.size = strlen(path);
    item.is_folder = (path[strlen(path) - 1] == '/');
    return ++keep_item_count;
}

int add_forbidden_item(const char *path) {
    char env_name[25];
    sprintf(env_name, "V_FORBID_ITEM_%d", forbidden_item_count);
    setenv(env_name, path, 1);
    forbidden_items = (PathItem *) realloc(forbidden_items,
                                           forbidden_item_count * sizeof(PathItem) +
                                           sizeof(PathItem));
    PathItem &item = forbidden_items[forbidden_item_count];
    item.path = strdup(path);
    item.size = strlen(path);
    return ++forbidden_item_count;
}

int add_replace_item(const char *orig_path, const char *new_path) {
    char src_env_name[25];
    char dst_env_name[25];
    sprintf(src_env_name, "V_REPLACE_ITEM_SRC_%d", replace_item_count);
    sprintf(dst_env_name, "V_REPLACE_ITEM_DST_%d", replace_item_count);
    setenv(src_env_name, orig_path, 1);
    setenv(dst_env_name, new_path, 1);

    replace_items = (ReplaceItem *) realloc(replace_items,
                                            replace_item_count * sizeof(ReplaceItem) +
                                            sizeof(ReplaceItem));
    ReplaceItem &item = replace_items[replace_item_count];
    item.orig_path = strdup(orig_path);
    item.orig_size = strlen(orig_path);
    item.new_path = strdup(new_path);
    item.new_size = strlen(new_path);
    item.is_folder = (orig_path[strlen(orig_path) - 1] == '/');
    return ++replace_item_count;
}


PathItem *get_keep_items() {
    return keep_items;
}

PathItem *get_forbidden_item() {
    return forbidden_items;
}

ReplaceItem *get_replace_items() {
    return replace_items;
}

int get_keep_item_count() {
    return keep_item_count;
}

int get_forbidden_item_count() {
    return forbidden_item_count;
}

int get_replace_item_count() {
    return replace_item_count;
}

inline bool match_path(bool is_folder, size_t size, char *item_path, char *path) {
    if (is_folder) {
        if (strlen(path) < size) {
            // ignore the last '/'
            return strncmp(item_path, path, size - 1) == 0;
        }
    }
    return strncmp(item_path, path, size) == 0;
}


const char *relocate_path(const char *_path, int *result) {
    if (_path == NULL) {
        *result = NOT_MATCH;
        return NULL;
    }
    char *path = canonicalize_filename(_path);
    for (int i = 0; i < forbidden_item_count; ++i) {
        PathItem &item = forbidden_items[i];
        if (match_path(item.is_folder, item.size, item.path, path)) {
            *result = FORBID;
            // Permission denied
            *__errno() = 13;
            free(path);
            return NULL;
        }
    }
    for (int i = 0; i < keep_item_count; ++i) {
        PathItem &item = keep_items[i];
        if (match_path(item.is_folder, item.size, item.path, path)) {
            *result = KEEP;
            return path;
        }
    }
    for (int i = 0; i < replace_item_count; ++i) {
        ReplaceItem &item = replace_items[i];
        if (match_path(item.is_folder, item.orig_size, item.orig_path, path)) {
            std::string redirect_path(item.new_path);
            redirect_path += path + item.orig_size;
            *result = MATCH;
            free(path);
            LOGD("[RD] %s", redirect_path.c_str());
            return strdup(redirect_path.c_str());
        }
    }
    *result = NOT_MATCH;
    return _path;
}
