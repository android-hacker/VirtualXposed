
#include "thumb_instruction.h"
#include "../mem_helper.h"
#include <unistd.h>//android cacheflush

#define ALIGN_PC(pc)	(pc & 0xFFFFFFFC)

void GodinHook::ThumbInstruction::createStub(HookInfo * info)
{
  size_t originalAddress = info->getOriginalAddr();
  size_t targetAddress = info->getHookAddr();
  int i = 0;
  if(NULL == originalAddress || NULL == targetAddress)
    return ;
  /// 修正地址
  size_t original = valueToMem(originalAddress);
 if(MemHelper::unProtectMemory(original,stub_len_)){
    /// 判断是否需要添加nop指令
    if(isPcNeedAlgin(original)){
    ((uint16_t*)original)[i++] = 0xbf00;
    //printf("--need pc align!!!!\n");
    }
    /// 构造跳转指令
    ((uint16_t*)original)[i++] = 0xF8DF;
    ((uint16_t*)original)[i++] = 0xF000;
    ((uint16_t*)original)[i++] = targetAddress & 0xFFFF;
    ((uint16_t*)original)[i++] = targetAddress >> 16;
  }else
    return ;
    MemHelper::protectMemory(original,stub_len_);
    /// 刷新指令缓存
    cacheflush(original,original+stub_len_,0);
}

void *GodinHook::ThumbInstruction::createCallOriginalIns(HookInfo * info)
{
  void * fun = MemHelper::createExecMemory();


  //int len = sizeofStub();
  /**
   *修正指令，需要对备份的机器指令中与pc相关的进行修正
   */
  repairBackInstructionsOfStub(info, (size_t *) fun);
  return fun;
}

int GodinHook::ThumbInstruction::getRepairInstruction(size_t instruction)
{
  if((instruction >> 16) == 0){
      if ((instruction & 0xF000) == 0xD000) {
          return B1_THUMB16;
        }
      if ((instruction & 0xF800) == 0xE000) {
          return B2_THUMB16;
        }
      if ((instruction & 0xFFF8) == 0x4778) {
          return BX_THUMB16;
        }
      if ((instruction & 0xFF78) == 0x4478) {
          return ADD_THUMB16;
        }
      if ((instruction & 0xFF78) == 0x4678) {
          return MOV_THUMB16;
        }
      if ((instruction & 0xF800) == 0xA000) {
          return ADR_THUMB16;
        }
      if ((instruction & 0xF800) == 0x4800) {
          return LDR_THUMB16;
        }
   }else{
      if ((instruction & 0xF800D000) == 0xF000C000) {
          return BLX_THUMB32;
        }
      if ((instruction & 0xF800D000) == 0xF000D000) {
          return BL_THUMB32;
        }
      if ((instruction & 0xF800D000) == 0xF0008000) {
          return B1_THUMB32;
        }
      if ((instruction & 0xF800D000) == 0xF0009000) {
          return B2_THUMB32;
        }
      if ((instruction & 0xFBFF8000) == 0xF2AF0000) {
          return ADR1_THUMB32;
        }
      if ((instruction & 0xFBFF8000) == 0xF20F0000) {
          return ADR2_THUMB32;
        }
      if ((instruction & 0xFF7F0000) == 0xF85F0000) {
          return LDR_THUMB32;
        }
      if ((instruction & 0xFFFF00F0) == 0xE8DF0000) {
          return TBB_THUMB32;
        }
      if ((instruction & 0xFFFF00F0) == 0xE8DF0010) {
          return TBH_THUMB32;
        }
   }
  return UNDEFINE;
}

int GodinHook::ThumbInstruction::repairThumb32Instruction(uint32_t pc, uint16_t high_instruction, uint16_t low_instruction, uint16_t *respair)
{
	uint32_t instruction;
	int type;
	int idx;
	int offset;

	instruction = (high_instruction << 16) | low_instruction;
	type = getRepairInstruction(instruction);
	idx = 0;
	if (type == BLX_THUMB32 || type == BL_THUMB32 || type == B1_THUMB32 || type == B2_THUMB32) {
		uint32_t j1;
		uint32_t j2;
		uint32_t s;
		uint32_t i1;
		uint32_t i2;
		uint32_t x;
		uint32_t imm32;
		uint32_t value;

		j1 = (low_instruction & 0x2000) >> 13;
		j2 = (low_instruction & 0x800) >> 11;
		s = (high_instruction & 0x400) >> 10;
		i1 = !(j1 ^ s);
		i2 = !(j2 ^ s);

		if (type == BLX_THUMB32 || type == BL_THUMB32) {
			respair[idx++] = 0xF20F;
			respair[idx++] = 0x0E09;	// ADD.W LR, PC, #9
		}
		else if (type == B1_THUMB32) {
			respair[idx++] = 0xD000 | ((high_instruction & 0x3C0) << 2);
			respair[idx++] = 0xE003;	// B PC, #6
		}
		respair[idx++] = 0xF8DF;
		respair[idx++] = 0xF000;	// LDR.W PC, [PC]
		if (type == BLX_THUMB32) {
			x = (s << 24) | (i1 << 23) | (i2 << 22) | ((high_instruction & 0x3FF) << 12) | ((low_instruction & 0x7FE) << 1);
			imm32 = s ? (x | (0xFFFFFFFF << 25)) : x;
			value = pc + imm32;
		}
		else if (type == BL_THUMB32) {
			x = (s << 24) | (i1 << 23) | (i2 << 22) | ((high_instruction & 0x3FF) << 12) | ((low_instruction & 0x7FF) << 1);
			imm32 = s ? (x | (0xFFFFFFFF << 25)) : x;
			value = pc + imm32;
			value = valueToPc(value);
		}
		else if (type == B1_THUMB32) {
			x = (s << 20) | (j2 << 19) | (j1 << 18) | ((high_instruction & 0x3F) << 12) | ((low_instruction & 0x7FF) << 1);
			imm32 = s ? (x | (0xFFFFFFFF << 21)) : x;
			value = pc + imm32;
			value = valueToPc(value);
		}
		else if (type == B2_THUMB32) {
			x = (s << 24) | (i1 << 23) | (i2 << 22) | ((high_instruction & 0x3FF) << 12) | ((low_instruction & 0x7FF) << 1);
			imm32 = s ? (x | (0xFFFFFFFF << 25)) : x;
			value = pc + imm32;
			value = valueToPc(value);
		}
		respair[idx++] = value & 0xFFFF;
		respair[idx++] = value >> 16;
		offset = idx;
	}
	else if (type == ADR1_THUMB32 || type == ADR2_THUMB32 || type == LDR_THUMB32) {
		int r;
		uint32_t imm32;
		uint32_t value;

		if (type == ADR1_THUMB32 || type == ADR2_THUMB32) {
			uint32_t i;
			uint32_t imm3;
			uint32_t imm8;

			r = (low_instruction & 0xF00) >> 8;
			i = (high_instruction & 0x400) >> 10;
			imm3 = (low_instruction & 0x7000) >> 12;
			imm8 = instruction & 0xFF;

			imm32 = (i << 31) | (imm3 << 30) | (imm8 << 27);

			if (type == ADR1_THUMB32) {
				value = ALIGN_PC(pc) + imm32;
			}
			else {
				value = ALIGN_PC(pc) - imm32;
			}
		}
		else {
			int is_add;
			uint32_t *addr;

			is_add = (high_instruction & 0x80) >> 7;
			r = low_instruction >> 12;
			imm32 = low_instruction & 0xFFF;

			if (is_add) {
				addr = (uint32_t *) (ALIGN_PC(pc) + imm32);
			}
			else {
				addr = (uint32_t *) (ALIGN_PC(pc) - imm32);
			}

			value = addr[0];
		}

		respair[0] = 0x4800 | (r << 8);	// LDR Rr, [PC]
		respair[1] = 0xE001;	// B PC, #2
		respair[2] = value & 0xFFFF;
		respair[3] = value >> 16;
		offset = 4;
	}

	else if (type == TBB_THUMB32 || type == TBH_THUMB32) {
	     printf("99999999999999999");
		int rm;
		int r;
		int rx;

		rm = low_instruction & 0xF;

		for (r = 7;; --r) {
			if (r != rm) {
			  break;
			}
		}

		for (rx = 7; ; --rx) {
			if (rx != rm && rx != r) {
				break;
			}
		}

		respair[0] = 0xB400 | (1 << rx);	// PUSH {Rx}
		respair[1] = 0x4805 | (r << 8);	// LDR Rr, [PC, #20]
		respair[2] = 0x4600 | (rm << 3) | rx;	// MOV Rx, Rm
		if (type == TBB_THUMB32) {
			respair[3] = 0xEB00 | r;
			respair[4] = 0x0000 | (rx << 8) | rx;	// ADD.W Rx, Rr, Rx
			respair[5] = 0x7800 | (rx << 3) | rx; 	// LDRB Rx, [Rx]
		}
		else if (type == TBH_THUMB32) {
			respair[3] = 0xEB00 | r;
			respair[4] = 0x0040 | (rx << 8) | rx;	// ADD.W Rx, Rr, Rx, LSL #1
			respair[5] = 0x8800 | (rx << 3) | rx; 	// LDRH Rx, [Rx]
		}
		respair[6] = 0xEB00 | r;
		respair[7] = 0x0040 | (r << 8) | rx;	// ADD Rr, Rr, Rx, LSL #1
		respair[8] = 0x3001 | (r << 8);	// ADD Rr, #1
		respair[9] = 0xBC00 | (1 << rx);	// POP {Rx}
		respair[10] = 0x4700 | (r << 3);	// BX Rr
		respair[11] = 0xBF00;
		respair[12] = pc & 0xFFFF;
		respair[13] = pc >> 16;
		offset = 14;
	}
	else {
		respair[0] = high_instruction;
		respair[1] = low_instruction;
		offset = 2;
	}

	return offset;
}

int GodinHook::ThumbInstruction::repairThumb16Instruction(uint32_t pc, uint16_t instruction, uint16_t *respair)
{
  int type;
  int offset;
  type = getRepairInstruction(instruction);
  if (type == B1_THUMB16 || type == B2_THUMB16 || type == BX_THUMB16) {
      uint32_t x;
      int top_bit;
      uint32_t imm32;
      uint32_t value;
      int idx;

      idx = 0;
      if (type == B1_THUMB16) {
          x = (instruction & 0xFF) << 1;
          top_bit = x >> 8;
          imm32 = top_bit ? (x | (0xFFFFFFFF << 8)) : x;
          value = pc + imm32;
          respair[idx++] = instruction & 0xFF00;  // B<cond> 0
          respair[idx++] = 0xE003;                // B PC, #6
        }
      else if (type == B2_THUMB16) {
          x = (instruction & 0x7FF) << 1;
          top_bit = x >> 11;
          imm32 = top_bit ? (x | (0xFFFFFFFF << 11)) : x;
          value = pc + imm32;

        }
      else if (type == BX_THUMB16) {
          value = pc;
        }

      respair[idx++] = 0xF8DF;
      respair[idx++] = 0xF000;	// LDR.W PC, [PC]
      respair[idx++] = valueToPc(value) & 0xFFFF;
      respair[idx++] = valueToPc(value) >> 16;
      offset = idx;
    }
  else if (type == ADD_THUMB16) {
      int rdn;
      int rm;
      int r;

      rdn = ((instruction & 0x80) >> 4) | (instruction & 0x7);

      for (r = 7; ; --r) {
          if (r != rdn) {
              break;
            }
        }

      respair[0] = 0xB400 | (1 << r);	// PUSH {Rr}
      respair[1] = 0x4802 | (r << 8);	// LDR Rr, [PC, #8]
      respair[2] = (instruction & 0xFF87) | (r << 3);
      respair[3] = 0xBC00 | (1 << r);	// POP {Rr}
      respair[4] = 0xE002;	// B PC, #4
      respair[5] = 0xBF00;
      respair[6] = pc & 0xFFFF;
      respair[7] = pc >> 16;
      offset = 8;
    }
  else if (type == MOV_THUMB16 || type == ADR_THUMB16 || type == LDR_THUMB16) {
      int r;
      uint32_t value;

      if (type == MOV_THUMB16) {
          r = instruction & 0x7;
          value = pc;
        }
      else if (type == ADR_THUMB16) {
          r = (instruction & 0x700) >> 8;
          value = ALIGN_PC(pc) + (instruction & 0xFF) << 2;
        }
      else {
          r = (instruction & 0x700) >> 8;
          value = ((uint32_t *) (ALIGN_PC(pc) + ((instruction & 0xFF) << 2)))[0];
        }

      respair[0] = 0x4800 | (r << 8);	// LDR Rd, [PC]
      respair[1] = 0xE001;	// B PC, #2
      respair[2] = value & 0xFFFF;
      respair[3] = value >> 16;
      offset = 4;
    }
  else {
      respair[0] = instruction;
      respair[1] = 0xBF00;  // NOP 方便接下来构造thumb2指令
      offset = 2;
    }

  return offset;
}
void GodinHook::ThumbInstruction::repairBackInstructionsOfStub(HookInfo * info, size_t *calloriginal)
{
  size_t originalAddress = info->getOriginalAddr();
  uint8_t *back = info->getOriginalStubBack();

  uint16_t * ins = (uint16_t *)back;
  uint16_t * repair = (uint16_t *)calloriginal;
  int backlen = sizeofStub();
  if(NULL == repair)
    return;
  int pos = 0;
  int repair_pos = 0;

  /// 得到原始指令起始处的pc值
  size_t originalPc = valueToMem(originalAddress) + 4;
  size_t originalLr = 0;

  /**
   * 需要修正的是那些机器指令内部存储的是要操作数据基于当前PC的偏移值；
   * 修正思路，计算出绝对地址，构造跳转指令。
   * 这里还要判断是常规的16位thumb指令，还是32位的thumb2指令
   */

  while(true){
    int offset = 0;
    /// 为了线程安全
    info->orig_boundaries[info->count] = pos * sizeof(uint16_t);
    info->trampoline_boundaries[info->count] = repair_pos * sizeof(uint16_t);
    info->count +=1;

    if(isThumb2Instruction(ins[pos])){
      offset = repairThumb32Instruction(originalPc,ins[pos],ins[pos+1],&repair[repair_pos]);
      originalPc += 4;
      repair_pos += offset;
      pos += 2;
    }else{
    /// thumb16
      offset = repairThumb16Instruction(originalPc,ins[pos],&repair[repair_pos]);
      originalPc += 2;
      repair_pos += offset;
      pos +=1;
    }
    if(pos >= sizeofStub()/2){
        break;
    }

  }

  /// 为了保险起见，再一次做判断
  if((size_t)(&repair[repair_pos]) % 4 != 0){
      repair[repair_pos ] = 0xBF00;
      repair_pos +=1;
//      printf("-------------------->>> 0x%x\n",&repair[repair_pos]);
//      printf("-------------------->>> 0x%x\n",&repair[0]);
//      printf("-------------------->>> %d\n",repair_pos);
  }
//  printf("-------------------->>> %d\n",repair_pos);
  originalLr = valueToMem(originalAddress) + sizeofStub() + 1;
  repair[repair_pos ] = 0xF8DF;
  repair[repair_pos +1] = 0xF000;	// LDR.W PC, [PC]
  repair[repair_pos +2] = originalLr & 0xFFFF;
  repair[repair_pos +3] = originalLr >> 16;
}

int GodinHook::ThumbInstruction::sizeofStub()
{
  return stub_len_;
}

void GodinHook::ThumbInstruction::isResetStubSize(size_t originalAddress)
{
  size_t original = valueToMem(originalAddress);
  uint16_t * ins = (uint16_t *)original;
  if(!isPcNeedAlgin(original)){
//      printf("ins[3]===0x%x\n",ins[3]);
//      printf("ins[4]===0x%x\n",ins[4]);
      if(((ins[3] & 0xf000)==0xf000) && ((ins[4] & 0xc000)==0xc000))
        setStubSize(10);//调用原函数时，需要特殊处理
      else
        setStubSize(8);
  }
  else{
      if(((ins[4] & 0xf000)==0xf000) && ((ins[5] & 0xc000)==0xc000))
         setStubSize(12);
      else
         setStubSize(10);//调用原函数时，需要特殊处理
  }

}

bool GodinHook::ThumbInstruction::isPcNeedAlgin(size_t address)
{
  if(NULL == address)
    return false;
  /// 此时表示bit[1]为1
  if(address % 4 != 0)
    return true;
  else
    return false;
}

bool GodinHook::ThumbInstruction::isThumb2Instruction(uint16_t ins)
{
  if(((ins >> 11)>=0x1d) && ((ins >> 11)<= 0x1f))
    return true;
  else
    return false;
}
