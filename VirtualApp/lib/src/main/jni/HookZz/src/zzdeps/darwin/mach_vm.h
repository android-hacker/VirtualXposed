#ifndef    _mach_vm_user_
#define    _mach_vm_user_

/* Module mach_vm */

#include <string.h>
#include <mach/ndr.h>
#include <mach/boolean.h>
#include <mach/kern_return.h>
#include <mach/notify.h>
#include <mach/mach_types.h>
#include <mach/message.h>
#include <mach/mig_errors.h>
#include <mach/port.h>

/* BEGIN MIG_STRNCPY_ZEROFILL CODE */

#if defined(__has_include)
#if __has_include(<mach/mig_strncpy_zerofill_support.h>)
#ifndef USING_MIG_STRNCPY_ZEROFILL
#define USING_MIG_STRNCPY_ZEROFILL
#endif
#ifndef __MIG_STRNCPY_ZEROFILL_FORWARD_TYPE_DECLS__
#define __MIG_STRNCPY_ZEROFILL_FORWARD_TYPE_DECLS__
#ifdef __cplusplus
extern "C" {
#endif
extern int mig_strncpy_zerofill(char *dest, const char *src, int len) __attribute__((weak_import));
#ifdef __cplusplus
}
#endif
#endif /* __MIG_STRNCPY_ZEROFILL_FORWARD_TYPE_DECLS__ */
#endif /* __has_include(<mach/mig_strncpy_zerofill_support.h>) */
#endif /* __has_include */

/* END MIG_STRNCPY_ZEROFILL CODE */


#ifdef AUTOTEST
#ifndef FUNCTION_PTR_T
#define FUNCTION_PTR_T
typedef void (*function_ptr_t)(mach_port_t, char *, mach_msg_type_number_t);
typedef struct {
        char            *name;
        function_ptr_t  function;
} function_table_entry;
typedef function_table_entry   *function_table_t;
#endif /* FUNCTION_PTR_T */
#endif /* AUTOTEST */

#ifndef    mach_vm_MSG_COUNT
#define    mach_vm_MSG_COUNT    20
#endif    /* mach_vm_MSG_COUNT */

#include <mach/std_types.h>
#include <mach/mig.h>
#include <mach/mig.h>
#include <mach/mach_types.h>
#include <mach_debug/mach_debug_types.h>

#ifdef __BeforeMigUserHeader
__BeforeMigUserHeader
#endif /* __BeforeMigUserHeader */

#include <sys/cdefs.h>

__BEGIN_DECLS


/* Routine mach_vm_allocate */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_allocate
        (
                vm_map_t target,
                mach_vm_address_t *address,
                mach_vm_size_t size,
                int flags
        );

/* Routine mach_vm_deallocate */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_deallocate
        (
                vm_map_t target,
                mach_vm_address_t address,
                mach_vm_size_t size
        );

/* Routine mach_vm_protect */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_protect
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                mach_vm_size_t size,
                boolean_t set_maximum,
                vm_prot_t new_protection
        );

/* Routine mach_vm_inherit */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_inherit
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                mach_vm_size_t size,
                vm_inherit_t new_inheritance
        );

/* Routine mach_vm_read */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_read
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                mach_vm_size_t size,
                vm_offset_t *data,
                mach_msg_type_number_t *dataCnt
        );

/* Routine mach_vm_read_list */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_read_list
        (
                vm_map_t target_task,
                mach_vm_read_entry_t data_list,
                natural_t count
        );

/* Routine mach_vm_write */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_write
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                vm_offset_t data,
                mach_msg_type_number_t dataCnt
        );

/* Routine mach_vm_copy */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_copy
        (
                vm_map_t target_task,
                mach_vm_address_t source_address,
                mach_vm_size_t size,
                mach_vm_address_t dest_address
        );

/* Routine mach_vm_read_overwrite */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_read_overwrite
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                mach_vm_size_t size,
                mach_vm_address_t data,
                mach_vm_size_t *outsize
        );

/* Routine mach_vm_msync */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_msync
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                mach_vm_size_t size,
                vm_sync_t sync_flags
        );

/* Routine mach_vm_behavior_set */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_behavior_set
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                mach_vm_size_t size,
                vm_behavior_t new_behavior
        );

/* Routine mach_vm_map */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_map
        (
                vm_map_t target_task,
                mach_vm_address_t *address,
                mach_vm_size_t size,
                mach_vm_offset_t mask,
                int flags,
                mem_entry_name_port_t object,
                memory_object_offset_t offset,
                boolean_t copy,
                vm_prot_t cur_protection,
                vm_prot_t max_protection,
                vm_inherit_t inheritance
        );

/* Routine mach_vm_machine_attribute */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_machine_attribute
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                mach_vm_size_t size,
                vm_machine_attribute_t attribute,
                vm_machine_attribute_val_t *value
        );

/* Routine mach_vm_remap */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_remap
        (
                vm_map_t target_task,
                mach_vm_address_t *target_address,
                mach_vm_size_t size,
                mach_vm_offset_t mask,
                int flags,
                vm_map_t src_task,
                mach_vm_address_t src_address,
                boolean_t copy,
                vm_prot_t *cur_protection,
                vm_prot_t *max_protection,
                vm_inherit_t inheritance
        );

/* Routine mach_vm_page_query */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_page_query
        (
                vm_map_t target_map,
                mach_vm_offset_t offset,
                integer_t *disposition,
                integer_t *ref_count
        );

/* Routine mach_vm_region_recurse */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_region_recurse
        (
                vm_map_t target_task,
                mach_vm_address_t *address,
                mach_vm_size_t *size,
                natural_t *nesting_depth,
                vm_region_recurse_info_t info,
                mach_msg_type_number_t *infoCnt
        );

/* Routine mach_vm_region */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_region
        (
                vm_map_t target_task,
                mach_vm_address_t *address,
                mach_vm_size_t *size,
                vm_region_flavor_t flavor,
                vm_region_info_t info,
                mach_msg_type_number_t *infoCnt,
                mach_port_t *object_name
        );

/* Routine _mach_make_memory_entry */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t _mach_make_memory_entry
        (
                vm_map_t target_task,
                memory_object_size_t *size,
                memory_object_offset_t offset,
                vm_prot_t permission,
                mem_entry_name_port_t *object_handle,
                mem_entry_name_port_t parent_handle
        );

/* Routine mach_vm_purgable_control */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_purgable_control
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                vm_purgable_t control,
                int *state
        );

/* Routine mach_vm_page_info */
#ifdef    mig_external
mig_external
#else
extern
#endif    /* mig_external */
kern_return_t mach_vm_page_info
        (
                vm_map_t target_task,
                mach_vm_address_t address,
                vm_page_info_flavor_t flavor,
                vm_page_info_t info,
                mach_msg_type_number_t *infoCnt
        );

__END_DECLS

/********************** Caution **************************/
/* The following data types should be used to calculate  */
/* maximum message sizes only. The actual message may be */
/* smaller, and the position of the arguments within the */
/* message layout may vary from what is presented here.  */
/* For example, if any of the arguments are variable-    */
/* sized, and less than the maximum is sent, the data    */
/* will be packed tight in the actual message to reduce  */
/* the presence of holes.                                */
/********************** Caution **************************/

/* typedefs for all requests */

#ifndef __Request__mach_vm_subsystem__defined
#define __Request__mach_vm_subsystem__defined

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
    int flags;
} __Request__mach_vm_allocate_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
} __Request__mach_vm_deallocate_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
    boolean_t set_maximum;
    vm_prot_t new_protection;
} __Request__mach_vm_protect_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
    vm_inherit_t new_inheritance;
} __Request__mach_vm_inherit_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
} __Request__mach_vm_read_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_read_entry_t data_list;
    natural_t count;
} __Request__mach_vm_read_list_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    /* start of the kernel processed data */
    mach_msg_body_t msgh_body;
    mach_msg_ool_descriptor_t data;
    /* end of the kernel processed data */
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_msg_type_number_t dataCnt;
} __Request__mach_vm_write_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t source_address;
    mach_vm_size_t size;
    mach_vm_address_t dest_address;
} __Request__mach_vm_copy_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
    mach_vm_address_t data;
} __Request__mach_vm_read_overwrite_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
    vm_sync_t sync_flags;
} __Request__mach_vm_msync_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
    vm_behavior_t new_behavior;
} __Request__mach_vm_behavior_set_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    /* start of the kernel processed data */
    mach_msg_body_t msgh_body;
    mach_msg_port_descriptor_t object;
    /* end of the kernel processed data */
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
    mach_vm_offset_t mask;
    int flags;
    memory_object_offset_t offset;
    boolean_t copy;
    vm_prot_t cur_protection;
    vm_prot_t max_protection;
    vm_inherit_t inheritance;
} __Request__mach_vm_map_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
    vm_machine_attribute_t attribute;
    vm_machine_attribute_val_t value;
} __Request__mach_vm_machine_attribute_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    /* start of the kernel processed data */
    mach_msg_body_t msgh_body;
    mach_msg_port_descriptor_t src_task;
    /* end of the kernel processed data */
    NDR_record_t NDR;
    mach_vm_address_t target_address;
    mach_vm_size_t size;
    mach_vm_offset_t mask;
    int flags;
    mach_vm_address_t src_address;
    boolean_t copy;
    vm_inherit_t inheritance;
} __Request__mach_vm_remap_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_offset_t offset;
} __Request__mach_vm_page_query_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    natural_t nesting_depth;
    mach_msg_type_number_t infoCnt;
} __Request__mach_vm_region_recurse_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    vm_region_flavor_t flavor;
    mach_msg_type_number_t infoCnt;
} __Request__mach_vm_region_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    /* start of the kernel processed data */
    mach_msg_body_t msgh_body;
    mach_msg_port_descriptor_t parent_handle;
    /* end of the kernel processed data */
    NDR_record_t NDR;
    memory_object_size_t size;
    memory_object_offset_t offset;
    vm_prot_t permission;
} __Request___mach_make_memory_entry_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    vm_purgable_t control;
    int state;
} __Request__mach_vm_purgable_control_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    mach_vm_address_t address;
    vm_page_info_flavor_t flavor;
    mach_msg_type_number_t infoCnt;
} __Request__mach_vm_page_info_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif
#endif /* !__Request__mach_vm_subsystem__defined */

/* union of all requests */

#ifndef __RequestUnion__mach_vm_subsystem__defined
#define __RequestUnion__mach_vm_subsystem__defined
union __RequestUnion__mach_vm_subsystem {
    __Request__mach_vm_allocate_t Request_mach_vm_allocate;
    __Request__mach_vm_deallocate_t Request_mach_vm_deallocate;
    __Request__mach_vm_protect_t Request_mach_vm_protect;
    __Request__mach_vm_inherit_t Request_mach_vm_inherit;
    __Request__mach_vm_read_t Request_mach_vm_read;
    __Request__mach_vm_read_list_t Request_mach_vm_read_list;
    __Request__mach_vm_write_t Request_mach_vm_write;
    __Request__mach_vm_copy_t Request_mach_vm_copy;
    __Request__mach_vm_read_overwrite_t Request_mach_vm_read_overwrite;
    __Request__mach_vm_msync_t Request_mach_vm_msync;
    __Request__mach_vm_behavior_set_t Request_mach_vm_behavior_set;
    __Request__mach_vm_map_t Request_mach_vm_map;
    __Request__mach_vm_machine_attribute_t Request_mach_vm_machine_attribute;
    __Request__mach_vm_remap_t Request_mach_vm_remap;
    __Request__mach_vm_page_query_t Request_mach_vm_page_query;
    __Request__mach_vm_region_recurse_t Request_mach_vm_region_recurse;
    __Request__mach_vm_region_t Request_mach_vm_region;
    __Request___mach_make_memory_entry_t Request__mach_make_memory_entry;
    __Request__mach_vm_purgable_control_t Request_mach_vm_purgable_control;
    __Request__mach_vm_page_info_t Request_mach_vm_page_info;
};
#endif /* !__RequestUnion__mach_vm_subsystem__defined */
/* typedefs for all replies */

#ifndef __Reply__mach_vm_subsystem__defined
#define __Reply__mach_vm_subsystem__defined

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    mach_vm_address_t address;
} __Reply__mach_vm_allocate_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
} __Reply__mach_vm_deallocate_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
} __Reply__mach_vm_protect_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
} __Reply__mach_vm_inherit_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    /* start of the kernel processed data */
    mach_msg_body_t msgh_body;
    mach_msg_ool_descriptor_t data;
    /* end of the kernel processed data */
    NDR_record_t NDR;
    mach_msg_type_number_t dataCnt;
} __Reply__mach_vm_read_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    mach_vm_read_entry_t data_list;
} __Reply__mach_vm_read_list_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
} __Reply__mach_vm_write_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
} __Reply__mach_vm_copy_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    mach_vm_size_t outsize;
} __Reply__mach_vm_read_overwrite_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
} __Reply__mach_vm_msync_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
} __Reply__mach_vm_behavior_set_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    mach_vm_address_t address;
} __Reply__mach_vm_map_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    vm_machine_attribute_val_t value;
} __Reply__mach_vm_machine_attribute_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    mach_vm_address_t target_address;
    vm_prot_t cur_protection;
    vm_prot_t max_protection;
} __Reply__mach_vm_remap_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    integer_t disposition;
    integer_t ref_count;
} __Reply__mach_vm_page_query_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    mach_vm_address_t address;
    mach_vm_size_t size;
    natural_t nesting_depth;
    mach_msg_type_number_t infoCnt;
    int info[19];
} __Reply__mach_vm_region_recurse_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    /* start of the kernel processed data */
    mach_msg_body_t msgh_body;
    mach_msg_port_descriptor_t object_name;
    /* end of the kernel processed data */
    NDR_record_t NDR;
    mach_vm_address_t address;
    mach_vm_size_t size;
    mach_msg_type_number_t infoCnt;
    int info[10];
} __Reply__mach_vm_region_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    /* start of the kernel processed data */
    mach_msg_body_t msgh_body;
    mach_msg_port_descriptor_t object_handle;
    /* end of the kernel processed data */
    NDR_record_t NDR;
    memory_object_size_t size;
} __Reply___mach_make_memory_entry_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    int state;
} __Reply__mach_vm_purgable_control_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif

#ifdef  __MigPackStructs
#pragma pack(4)
#endif
typedef struct {
    mach_msg_header_t Head;
    NDR_record_t NDR;
    kern_return_t RetCode;
    mach_msg_type_number_t infoCnt;
    int info[32];
} __Reply__mach_vm_page_info_t __attribute__((unused));
#ifdef  __MigPackStructs
#pragma pack()
#endif
#endif /* !__Reply__mach_vm_subsystem__defined */

/* union of all replies */

#ifndef __ReplyUnion__mach_vm_subsystem__defined
#define __ReplyUnion__mach_vm_subsystem__defined
union __ReplyUnion__mach_vm_subsystem {
    __Reply__mach_vm_allocate_t Reply_mach_vm_allocate;
    __Reply__mach_vm_deallocate_t Reply_mach_vm_deallocate;
    __Reply__mach_vm_protect_t Reply_mach_vm_protect;
    __Reply__mach_vm_inherit_t Reply_mach_vm_inherit;
    __Reply__mach_vm_read_t Reply_mach_vm_read;
    __Reply__mach_vm_read_list_t Reply_mach_vm_read_list;
    __Reply__mach_vm_write_t Reply_mach_vm_write;
    __Reply__mach_vm_copy_t Reply_mach_vm_copy;
    __Reply__mach_vm_read_overwrite_t Reply_mach_vm_read_overwrite;
    __Reply__mach_vm_msync_t Reply_mach_vm_msync;
    __Reply__mach_vm_behavior_set_t Reply_mach_vm_behavior_set;
    __Reply__mach_vm_map_t Reply_mach_vm_map;
    __Reply__mach_vm_machine_attribute_t Reply_mach_vm_machine_attribute;
    __Reply__mach_vm_remap_t Reply_mach_vm_remap;
    __Reply__mach_vm_page_query_t Reply_mach_vm_page_query;
    __Reply__mach_vm_region_recurse_t Reply_mach_vm_region_recurse;
    __Reply__mach_vm_region_t Reply_mach_vm_region;
    __Reply___mach_make_memory_entry_t Reply__mach_make_memory_entry;
    __Reply__mach_vm_purgable_control_t Reply_mach_vm_purgable_control;
    __Reply__mach_vm_page_info_t Reply_mach_vm_page_info;
};
#endif /* !__RequestUnion__mach_vm_subsystem__defined */

#ifndef subsystem_to_name_map_mach_vm
#define subsystem_to_name_map_mach_vm \
    { "mach_vm_allocate", 4800 },\
    { "mach_vm_deallocate", 4801 },\
    { "mach_vm_protect", 4802 },\
    { "mach_vm_inherit", 4803 },\
    { "mach_vm_read", 4804 },\
    { "mach_vm_read_list", 4805 },\
    { "mach_vm_write", 4806 },\
    { "mach_vm_copy", 4807 },\
    { "mach_vm_read_overwrite", 4808 },\
    { "mach_vm_msync", 4809 },\
    { "mach_vm_behavior_set", 4810 },\
    { "mach_vm_map", 4811 },\
    { "mach_vm_machine_attribute", 4812 },\
    { "mach_vm_remap", 4813 },\
    { "mach_vm_page_query", 4814 },\
    { "mach_vm_region_recurse", 4815 },\
    { "mach_vm_region", 4816 },\
    { "_mach_make_memory_entry", 4817 },\
    { "mach_vm_purgable_control", 4818 },\
    { "mach_vm_page_info", 4819 }
#endif

#ifdef __AfterMigUserHeader
__AfterMigUserHeader
#endif /* __AfterMigUserHeader */

#endif     /* _mach_vm_user_ */
