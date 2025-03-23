package example.handler;

import me.adversing.nihil.intf.IPropertyHandler;
import example.Address;
import example.AddressDTO;

public class AddressDTOHandler implements IPropertyHandler<AddressDTO> {
    
    @Override
    public Object process(AddressDTO dto) {
        if (dto == null) {
            return null;
        }
        
        System.out.println("AddressDTOHandler: Converting AddressDTO to Address");
        
        Address address = new Address();
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        
        return address;
    }
} 