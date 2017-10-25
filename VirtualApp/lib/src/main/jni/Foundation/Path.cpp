#include "Path.h"
#define MAX_PATH_SIZE 4096

/* returns last slash position in @s or -1 if there is no one */
int get_last_slash_pos(char *s) {
    int last_slash = -1;
    char *slash = strrchr(s, '/');
    if (slash)
        last_slash = slash - s;
    return last_slash;
}

char *canonicalize_filename(const char *str) {
    int prev_last_slash = -1;
    int last_slash = -1;
    int i = 0;
    int j = 0;
    char c;
    char cprev = '\0';
    char result[MAX_PATH_SIZE] = {0};

    if (!str)
        return NULL;

    for (; i < MAX_PATH_SIZE && str[i]; ++i) {
        c = str[i];

        switch (c) {
            case '/':
                if (cprev == '/'/** || j == 0*/) {
                    // eat repeating and leading slashes
                    ;
                } else {
                    result[j++] = c;
                    prev_last_slash = last_slash;
                    last_slash = j - 1;
                }
                break;
            case '.':
                if (cprev == '.') {
                    int start_position = 0;
                    if (prev_last_slash > 0) {
                        start_position = prev_last_slash;
                        // handle following duplicate slash on next iteration, if any
                        cprev = '/';
                    }
                    while (j > start_position)
                        result[j--] = '\0';
                    result[j] = '\0';

                    // we lost last slash positions, calculate them
                    prev_last_slash = -1;
                    last_slash = get_last_slash_pos(result);
                    if (last_slash != -1) {
                        // trying to find previous last slash position
                        result[last_slash] = ' ';
                        prev_last_slash = get_last_slash_pos(result);
                        result[last_slash] = '/';
                    }
                } else {
                    // assume it is a valid to have dot in names
                    result[j++] = c;
                }
                break;
            default:
                result[j++] = c;
                break;
        }
        cprev = c;
    }
    return strndup(result, MAX_PATH_SIZE - 1);
}