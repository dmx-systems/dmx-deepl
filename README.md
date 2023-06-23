# DMX DeepL

The [DeepL](https://www.deepl.com/) API as a [DMX platform](https://github.com/dmx-systems/dmx-platform) service

## Config Properties

| Property           | Required | Description                                               |
| --------           | -------- | -----------                                               |
| dmx.deepl.base_url | no       | DeepL API base URL. Includes version number, ends with `/`.<br>Default is `https://api-free.deepl.com/v2/`.<br>For the payed plan use `https://api.deepl.com/v2/` instead. |
| dmx.deepl.auth_key | yes      | Your DeepL API key as obtained from https://www.deepl.com |

## Version History

**1.0** -- Jun 16, 2023

* 2 service calls:
    * translate()
    * usageStats() (RESTful)
* Compatible with DMX 5.3
